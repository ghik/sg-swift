/**
 * Copyright (C) 2006 - 2012
 *   Pawel Kedzior
 *   Tomasz Kmiecik
 *   Kamil Pietak
 *   Krzysztof Sikora
 *   Adam Wos
 *   Lukasz Faber
 *   Daniel Krzywicki
 *   and other students of AGH University of Science and Technology.
 *
 * This file is part of AgE.
 *
 * AgE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AgE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AgE.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Created: 2012-04-07
 * $Id: AggregateActionService.java 471 2012-10-30 11:17:00Z faber $
 */

package org.jage.agent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jage.action.Action;
import org.jage.action.ActionPhase;
import org.jage.action.AgentAction;
import org.jage.action.IActionContext;
import org.jage.action.IPerformActionStrategy;
import org.jage.action.SingleAction;
import org.jage.action.context.AddAgentActionContext;
import org.jage.action.context.AgentActionContext;
import org.jage.action.context.GetAgentActionContext;
import org.jage.action.context.IActionWithAgentReferenceContext;
import org.jage.action.context.KillAgentActionContext;
import org.jage.action.context.MoveAgentActionContext;
import org.jage.action.context.PassToParentActionContext;
import org.jage.action.context.RemoveAgentActionContext;
import org.jage.action.context.SendMessageActionContext;
import org.jage.action.ordering.ActionComparator;
import org.jage.action.ordering.DefaultActionComparator;
import org.jage.address.agent.AgentAddress;
import org.jage.address.selector.BroadcastSelector;
import org.jage.address.selector.IAddressSelector;
import org.jage.address.selector.agent.ParentAgentAddressSelector;
import org.jage.communication.message.IMessage;
import org.jage.platform.component.exception.ComponentException;
import org.jage.platform.component.provider.IComponentInstanceProvider;
import org.jage.util.RebuildablePriorityQueue;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * The action service for the default {@link SimpleAggregate}.
 *
 * @since 2.6
 * @author AGH AgE Team
 */
public class AggregateActionService {

	private static final Logger log = LoggerFactory.getLogger(AggregateActionService.class);

	/**
	 * The cache of methods containing actions' performing code (execute in ActionPhase.MAIN) <BR>
	 * Key: class of aggregate (Class) <BR>
	 * Value: map of available actions (action name, method) (Map)
	 */
	private static final Map<Class<?>, Map<String, Map<ActionPhase, Method>>> actionMethods = newHashMap();

	private SimpleAggregate aggregate;

	private IComponentInstanceProvider instanceProvider;

	protected final RebuildablePriorityQueue<Action> actionQueue = RebuildablePriorityQueue
	        .createWithComparator(new DefaultActionComparator());

	/**
	 * Enqueues a new action for later execution.
	 * <p>
	 *
	 * @param action
	 *            the action to enqueue.
	 */
	public void doAction(final Action action) {
		actionQueue.add(action);
	}

	/**
	 * A default implementation of action validation. It checks if used addresses point to agents in current aggregate.
	 *
	 * @param action
	 *            single action to validate
	 * @return collection of agent addresses used in action, if no addresses is used (action has selectors such as
	 *         {@link BroadcastSelector}, etc.) the empty collection is returned; <code>null</code> is returned when
	 *         action didn't validate addresses - then a default validation is performed
	 * @throws AgentException
	 *             when validation fails.
	 */
	public Collection<AgentAddress> validateAction(final SingleAction action) {
		checkNotNull(action);
		final Collection<AgentAddress> used = newHashSet();
		final IAddressSelector<AgentAddress> selector = action.getTarget();

		if (selector instanceof ParentAgentAddressSelector) {
			if (aggregate.getAgentsAddresses().contains(((ParentAgentAddressSelector)selector).getChildAddress())) {
				used.add(aggregate.getAddress());
			}
		} else {
			for (final AgentAddress address : selector.addresses()) {
				if (aggregate.getAgent(address) == null) {
					throw agentException("Agent %s does not exist in: %s. Action %s could not be performed.", address,
					        aggregate, action);
				}
				// add address to used collection (only if agent exists in aggregate aggregate)
				used.add(address);
			}
		}
		return used;
	}

	/**
	 * Processes all enqueued actions.
	 */
	public void processActions() {
		Action action;
		while ((action = actionQueue.poll()) != null) {
			try {
				processAction(action);
			} catch (final AgentException e) {
				log.error("Action could not be executed.", e);
			}
		}
	}

	/**
	 * Executes an action.
	 *
	 * @param action
	 *            the action to process.
	 *
	 * @throws AgentException
	 *             occurs when the requested action cannot be found or executed.
	 */
	protected void processAction(final Action action) {
		// phase I: initialization and validation
		final Collection<AgentAddress> used = initializeAction(action);

		// address generation
		initializeActionTargets(action, aggregate.getAgentsAddresses(), used);

		// phase II: perform main action part
		executeAction(action);

		// phase III: finalization
		finalizeAction(action);
	}

	protected Collection<AgentAddress> initializeAction(final Action action) {
		final Collection<AgentAddress> used = newHashSet();

		for (final SingleAction singleAction : action) {
			final IActionContext context = singleAction.getContext();
			final String actionName = retrieveActionName(singleAction);

			// addresses used by single action
			Collection<AgentAddress> addressesUsedByAction = null;

			/*
			 * First look for a strategic implementation of the action. If aggregate is not found, the action's
			 * implementation is looked up in the aggregate.
			 */
			final IPerformActionStrategy actionStrategy = retrieveActionStrategyImplementation(context, actionName);
			if (actionStrategy != null) {
				addressesUsedByAction = actionStrategy.init(aggregate, singleAction);
			} else {
				Method actionMethod = null;
				try {
					actionMethod = getActionMethod(getClass(), actionName, ActionPhase.INIT);
				} catch (final AgentException e) {
					// action is not a strategy and has no aggregates methods
					// defined (in all phases).
					handleUnknownAction(actionName, context);
				}

				if (actionMethod != null) {
					addressesUsedByAction = processActionContextMethod(actionMethod, singleAction);
				}
			}

			if (addressesUsedByAction == null) { // if no validation done yet, do
				// default validation now
				addressesUsedByAction = validateAction(singleAction);
			}

			assert (addressesUsedByAction != null);
			used.addAll(addressesUsedByAction);
		}

		return used;
	}

	/**
	 * Initializes all address selectors in action using all and used collections of agent addresses. For more details
	 * see {@link IAddressSelector#initialize(Collection, Collection)}.
	 *
	 * @param action
	 *            action which contains selectors to initialize
	 * @param all
	 *            collection of all available addresses
	 * @param used
	 *            collection of all used addresses
	 */
	protected void initializeActionTargets(final Action action, final Collection<AgentAddress> all,
	        final Collection<AgentAddress> used) {

		assert (action != null && all != null && used != null);

		for (final SingleAction singleAction : action) {
			singleAction.getTarget().initialize(all, used);
		}
	}

	/**
	 * Performs action. If action is a complex action, it iterates over all single actions and performs them.
	 *
	 * @param action
	 *            action to perform
	 */
	protected void executeAction(final Action action) {
		assert (action != null);
		for (final SingleAction singleAction : action) {
			for (final IAgent targetAgent : getTargetAgentsForAction(singleAction)) {
				final String actionName = retrieveActionName(singleAction);
				final IActionContext context = singleAction.getContext();

				/*
				 * First look for a strategic implementation of the action. If aggregate is not found, the action's
				 * implementation is looked up in the aggregate.
				 */
				final IPerformActionStrategy actionStrategy = retrieveActionStrategyImplementation(context, actionName);
				if (actionStrategy != null) {
					try {
						actionStrategy.perfom(targetAgent, context);
					} catch (final RuntimeException e) {
						throw new AgentException("An exception occured during performing action " + action, e);
					}
				} else {
					Method actionMethod = null;
					try {
						actionMethod = getActionMethod(getClass(), actionName, ActionPhase.MAIN);
					} catch (final AgentException e) {
						handleUnknownAction(actionName, context);
					}
					if (actionMethod != null) {
						try {
							processActionContextMethod(actionMethod, targetAgent, context);
						} catch (final RuntimeException e) {
							throw agentException("An exception occured during performing action %s.", action, e);
						}
					} else {
						performDefaultAction(actionName, targetAgent, context);
					}
				}
			}
		}
	}

	protected void finalizeAction(final Action action) {
		assert (action != null);
		for (final SingleAction singleAction : action) {
			for (final IAgent targetAgent : getTargetAgentsForAction(singleAction)) {
				final IActionContext context = singleAction.getContext();
				final String actionName = retrieveActionName(singleAction);

				/*
				 * First look for a strategic implementation of the action. If aggregate is not found, the action's
				 * implementation is looked up in the aggregate.
				 */
				final IPerformActionStrategy actionStrategy = retrieveActionStrategyImplementation(context, actionName);
				if (actionStrategy != null) {
					actionStrategy.finish(aggregate, context);
				} else {
					Method actionMethod = null;
					try {
						actionMethod = getActionMethod(getClass(), actionName, ActionPhase.FINISH);
					} catch (final AgentException e) {
						handleUnknownAction(actionName, context);
					}
					if (actionMethod != null) {
						processActionContextMethod(actionMethod, targetAgent, context);
					} else {
						finishDefaultAction(context, actionName);
					}
				}
			}
		}
	}

	/**
	 * A default implementation that handles unknown actions finalization in the aggregate. It does nothing.
	 *
	 * @param context
	 *            action context
	 * @param actionName
	 *            action name
	 */
	protected void finishDefaultAction(final IActionContext context, final String actionName) {
		// do nothing
	}

	/**
	 * A default implementation that handles unknown actions in the aggregate. Does nothing. May be overridden by
	 * subclasses.
	 *
	 * @param actionName
	 *            name of unknown action
	 * @param targetAgent
	 *            agent to invoke action on
	 * @param context
	 *            action context
	 * @throws AgentException
	 *             by default
	 */
	protected void performDefaultAction(final String actionName, final IAgent targetAgent, final IActionContext context) {
		// do nothing
	}

	/**
	 * A method which handles unknown (not found as strategy and aggregate's methods) action. Throws exception. May be
	 * overridden by subclasses to modify default behavior.
	 *
	 * @param actionName
	 *            action name
	 * @param context
	 *            action context
	 * @throws AgentException
	 */
	protected void handleUnknownAction(final String actionName, final IActionContext context) throws AgentException {
		throw agentException("The action cannot be found [name: %s]", actionName);
	}

	/**
	 * Determine name of action that should be executed. If context is annotated with one action only, aggregate is
	 * obvious. If it's annotated with more than one action, we need to check if an action to execute is specified and
	 * if it is on the list of supported actions
	 *
	 * @param singleAction
	 *            the single action
	 * @param context
	 *            the context
	 * @param contextAnnotation
	 *            the context annotation
	 *
	 * @return single string with name of action to execute
	 *
	 * @throws AgentException
	 *             the agent exception
	 */
	private static String retrieveActionName(final SingleAction singleAction) {
		assert (singleAction != null);
		final IActionContext context = singleAction.getContext();
		final AgentActionContext contextAnnotation = context.getClass().getAnnotation(AgentActionContext.class);
		if (contextAnnotation == null) {
			throw agentException(
			        "ActionContext class [%s] isn't annotated correctly. ActionContext require to be annotated by AgentActionContext",
			        context.getClass().toString());
		}

		final String[] actionNames = contextAnnotation.value();
		if (actionNames.length == 1) {
			// context is annotated with 1 action only - simple case
			return actionNames[0];
		}
		// context is multiannotated
		final String actionToExecute = singleAction.getActionToExecute();
		if (actionToExecute == null) {
			throw agentException("ActionContext %s is annotated with more than one action name, but no action"
			        + " was specified to be executed in the SingleAction object", context.getClass().toString());
		}
		for (final String name : actionNames) {
			if (actionToExecute.equals(name)) {
				return actionToExecute;
			}
		}
		throw agentException("ActionContext %s annotation does not support execution of action '%s'.", context
		        .getClass().toString(), actionToExecute);
	}

	@SuppressWarnings("unchecked")
	private <T> T processActionContextMethod(final Method actionMethod, final Object... arguments) {
		try {
			/*
			 * Protected and package action methods declared in aggregates in a different package will not be accessible
			 * by default - aggregate is fixed below by setting the method's accessible attribute.
			 */
			if (Modifier.isProtected(actionMethod.getModifiers()) || actionMethod.getModifiers() == 0) {
				actionMethod.setAccessible(true);
			}
			return (T)actionMethod.invoke(this, arguments);
		} catch (final IllegalAccessException e) {
			throw agentException("Cannot execute the action.", e);
		} catch (final IllegalArgumentException e) {
			final Class<?>[] params = actionMethod.getParameterTypes();

			final StringBuffer buffer = new StringBuffer();
			buffer.append("Cannot execute the action: ");
			// buffer.append(actionName);
			buffer.append(" method: ");
			buffer.append(actionMethod.getName());
			buffer.append("(");
			for (final Class<?> param : params) {
				buffer.append(param.getSimpleName());
				buffer.append(", ");
			}
			buffer.append(")");
			buffer.append(" arguments: ");
			for (final Object argument : arguments) {
				buffer.append(argument.getClass().getSimpleName());
				buffer.append(", ");
			}
			log.error(buffer.toString());
			throw agentException(buffer.toString(), e);
		} catch (final InvocationTargetException e) {
			throw agentException("Cannot execute the action.", e);
		}
	}

	/**
	 * actionContext points on action which will be executed without using Aggregate's method (implementation will be
	 * provided by ResourceManager).
	 *
	 * @param targetAgent
	 *            the target agent
	 * @param context
	 *            the context
	 * @param actionName
	 *            the action name
	 *
	 * @throws AgentException
	 *             the agent exception
	 */
	private IPerformActionStrategy retrieveActionStrategyImplementation(final IActionContext context,
	        final String actionName) {

		final Object obj = instanceProvider.getInstance(actionName);
		if (obj == null) {
			return null;
		}
		if (!(obj instanceof IPerformActionStrategy)) {
			throw agentException("Requested object %s is not an action implementation", actionName);
		}

		return (IPerformActionStrategy)obj;
	}

	/**
	 * Executes the action of sending a message to an agent.
	 *
	 * @param target
	 *            the target
	 * @param context
	 *            the context
	 */
	@AgentAction(name = SendMessageActionContext.ACTION_NAME)
	protected void performSendMessageAction(final ISimpleAgent target, final IActionContext context) {
		assert (context instanceof SendMessageActionContext);

		final SendMessageActionContext action = (SendMessageActionContext)context;
		final AgentAddress destAddress = target.getAddress();
		final IMessage<AgentAddress, ?> message = action.getMessage();

		target.deliverMessage(message);
		log.info("Message delivered to the agent {}.", destAddress);
	}

	/**
	 * Returns the method which contains the code of the given action in specified phase.
	 *
	 * @param aggrClass
	 *            the class of aggregate on which the action is to be executed
	 * @param actionName
	 *            the name of the action
	 * @param phase
	 *            action phase
	 *
	 * @return the method which contains the code of the given action or <code>null</code> if method does not exists in
	 *         given phase
	 *
	 * @throws AgentException
	 *             occurs when no method is defined for action in all phases
	 */
	private static Method getActionMethod(final Class<?> aggrClass, final String actionName, final ActionPhase phase) {
		Map<String, Map<ActionPhase, Method>> methodMap = actionMethods.get(aggrClass);
		if (methodMap == null) {
			methodMap = getActionMethods(aggrClass);
			actionMethods.put(aggrClass, methodMap);
		}

		final Map<ActionPhase, Method> actionMap = methodMap.get(actionName);

		if (actionMap == null) {
			throw agentException("The action cannot be found [name: %s]", actionName);
		}
		return actionMap.get(phase);
	}

	/**
	 * Returns the map of action methods of the given class.
	 *
	 * @param aggrClass
	 *            the class of an aggregate
	 *
	 * @return the map (action name, method) of action methods of the given class
	 */
	private static Map<String, Map<ActionPhase, Method>> getActionMethods(final Class<? extends Object> aggrClass) {

		final Map<String, Map<ActionPhase, Method>> result = newHashMap();
		// annotation mechanism for reading actions
		final Method[] methods = aggrClass.getDeclaredMethods();
		for (final Method method : methods) {
			final AgentAction action = method.getAnnotation(AgentAction.class);
			if (action != null) {
				final String name = action.name();
				final ActionPhase phase = action.phase();
				Map<ActionPhase, Method> actionMap = result.get(name);
				if (actionMap == null) {
					actionMap = newHashMap();
				}
				actionMap.put(phase, method);
				result.put(name, actionMap);
				log.debug("Method: {} added", action.name());
			}
		}
		if (!AggregateActionService.class.equals(aggrClass)) {
			result.putAll(getActionMethods(aggrClass.getSuperclass()));
		}

		return result;
	}

	/**
	 * Executes the action of adding an agent to aggregate aggregate.
	 *
	 * @param context
	 *            context of the action
	 */
	@AgentAction(name = AddAgentActionContext.ACTION_NAME, phase = ActionPhase.INIT)
	protected void performAddAgentAction(final SingleAction action) {
		assert (action != null && action.getContext() instanceof AddAgentActionContext);
		final AddAgentActionContext addContext = (AddAgentActionContext)action.getContext();
		aggregate.add(addContext.getAgent());
	}

	/**
	 * Executes the action of removing an agent from aggregate aggregate.
	 *
	 * @param target
	 *            agent to be removed
	 * @param context
	 *            context of the action
	 */
	@AgentAction(name = RemoveAgentActionContext.ACTION_NAME)
	protected void performRemoveAgentAction(final ISimpleAgent target, final IActionContext context) {
		assert (target != null && context != null && context instanceof AddAgentActionContext);
		try {
			aggregate.removeAgent(target.getAddress());
		} catch (final AgentException e) {
			log.error("Cannot remove the agent [agent: {}, parent: {}]",
			        new Object[] { target.getAddress(), aggregate.getAddress() }, e);
		}
	}

	/**
	 * Perform kill agent action.
	 *
	 * @param target
	 *            the target
	 * @param context
	 *            the context
	 */
	@AgentAction(name = KillAgentActionContext.ACTION_NAME, phase = ActionPhase.FINISH)
	protected void performKillAgentAction(final ISimpleAgent target, final IActionContext context) {
		try {
			aggregate.removeAgent(target.getAddress());
			target.finish();
		} catch (final AgentException e) {
			log.info("Cannot kill the agent [agent: {}, parent: {}]", target.getAddress(), aggregate.getAddress());
		} catch (final ComponentException e) {
			// FIXME Should we use IStatefulComponent.finish() there?
			log.info("Cannot kill the agent [agent: " + target.getAddress() + ", parent: " + aggregate.getAddress()
			        + "]");
		}
	}

	/**
	 * Part of moving action. Removing an agent.
	 *
	 * @param action
	 *            the action
	 * @param target
	 *            target
	 *
	 * @throws AgentException
	 *             if fails
	 */
	private static void performMoveAgentActionRemove(final MoveAgentActionContext action, final ISimpleAgent target)
	        throws AgentException {
		try {
			final ISimpleAggregate parent = action.getParent();
			parent.removeAgent(action.getAgent().getAddress());
		} catch (final AgentException e) {
			log.info("Cannot remove the agent [agent: {}, parent: {}]", action.getAgent().getAddress(), action
			        .getParent().getAddress());
			throw e;
		}
	}

	/**
	 * Adds the agent to target aggregate.
	 *
	 * @param action
	 *            an action
	 * @param target
	 *            target
	 *
	 * @throws AgentException
	 *             when the operation must be rolled back
	 */
	private static void performMoveAgentActionAdd(final MoveAgentActionContext action, final ISimpleAggregate target)
	        throws AgentException {
		target.add(action.getAgent());
	}

	/**
	 * Adds an agent back to its aggregate.
	 *
	 * @param action
	 *            an action
	 * @param cause
	 *            the cause
	 */
	private static void performMoveAgentActionRollback(final MoveAgentActionContext action, final Throwable cause) {
		action.getParent().add(action.getAgent());
		log.info("Agent {} added back to his parent {}.", action.getAgent(), action.getParent());
	}

	/**
	 * Perform move agent action.
	 *
	 * @param target
	 *            the target
	 * @param context
	 *            the context
	 */
	@AgentAction(name = MoveAgentActionContext.ACTION_NAME)
	protected void performMoveAgentAction(final ISimpleAgent target, final IActionContext context) {
		assert (target != null && context instanceof MoveAgentActionContext);
		final MoveAgentActionContext moveContext = (MoveAgentActionContext)context;
		if (moveContext.getAgent() == null) {
			log.info("Cannot remove the agent");
			return;
		}
		if (!(target instanceof IAggregate)) {
			log.info("Cannot move the agent. "
			        + "The target parent is not an aggregate [agent: {}, parent: {}, destination: {}]", new Object[] {
			        moveContext.getAgent().getAddress(), moveContext.getParent().getAddress(), target.getAddress() });
			return;
		}

		try {
			performMoveAgentActionRemove(moveContext, target);
		} catch (final AgentException ae) {
			return; // It's okay to eat this exception.
		}

		try {
			performMoveAgentActionAdd(moveContext, (ISimpleAggregate)target);
		} catch (final AgentException ae) {
			// It's okay to eat this exception.
			performMoveAgentActionRollback(moveContext, ae);
		}
	}

	/**
	 * Gets agent's reference and its parent and save it into context The action can be used in action such as
	 * MoveAgentAction.
	 *
	 * @param target
	 *            the target
	 * @param context
	 *            the context
	 */
	@AgentAction(name = GetAgentActionContext.ACTION_NAME)
	protected void performGetAgentAction(final ISimpleAgent target, final IActionContext context) {
		assert (target != null && context instanceof GetAgentActionContext);
		final GetAgentActionContext action = (GetAgentActionContext)context;
		final IActionWithAgentReferenceContext awarc = action.getActionWithAgentReferenceContext();
		awarc.setAgent(target);
		awarc.setParent(aggregate);
	}

	/**
	 * Passes action held in a context to parent agent.
	 *
	 * @param target
	 *            this agent
	 * @param context
	 *            action context
	 * @throws AgentException
	 *             when agent environment is <code>null</code> or does not implement {@link ISimpleAgentEnvironment}
	 */
	@AgentAction(name = PassToParentActionContext.ACTION_NAME)
	protected void performPassToParentAction(final ISimpleAgent target, final PassToParentActionContext context) {
		if (aggregate.getAgentEnvironment() != null) {
			aggregate.getAgentEnvironment().submitAction(context.getAction());
		} else {
			throw agentException(
			        "Cannot pass action %1$s to environment - it is not instance of ISimpleAgentEnvironment.",
			        context.getAction());
		}
	}

	private List<IAgent> getTargetAgentsForAction(final SingleAction singleAction) {
		assert (singleAction != null);
		final IAddressSelector<AgentAddress> selector = singleAction.getTarget();
		final List<IAgent> targets = newArrayList();

		if (selector instanceof ParentAgentAddressSelector) {
			if (aggregate.getAgentsAddresses().contains(((ParentAgentAddressSelector)selector).getChildAddress())) {
				targets.add(aggregate);
			}
		} else {
			for (final AgentAddress target : selector.addresses()) {
				final IAgent targetAgent = aggregate.getAgent(target);

				if (targetAgent == null) {
					throw agentException("Target agent %s does not exists even if validation proccess was passed.",
					        target);
				}
				targets.add(targetAgent);
			}
		}
		return targets;
	}

	/**
	 * Sets the aggregate that owns this service instance.
	 *
	 * @param aggregate
	 *            the owner.
	 */
	public void setAggregate(final SimpleAggregate aggregate) {
		this.aggregate = checkNotNull(aggregate);
	}

	/**
	 * Sets the instance provider. It should be the instance provider of the owning aggregate or its child!
	 *
	 * @param provider
	 *            the instance provider.
	 */
	public void setInstanceProvider(final IComponentInstanceProvider provider) {
		this.instanceProvider = checkNotNull(provider);
		final ActionComparator comparator = instanceProvider.getInstance(ActionComparator.class);
		if (comparator != null) {
			actionQueue.setComparator(comparator);
		}
	}

	private static AgentException agentException(final String message, final Object... parameters) {
		final ArrayList<Object> parametersAsList = Lists.newArrayList(parameters);
		final int lastIndex = parametersAsList.size() - 1;
		final Object last = parametersAsList.get(lastIndex);
		if (last instanceof Throwable) {
			return new AgentException(String.format(message, parametersAsList.subList(0, lastIndex).toArray()),
			        (Throwable)last);
		}
		return new AgentException(String.format(message, parameters));
	}
}
