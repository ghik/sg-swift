'''
Created on 16-01-2013

@author: ghik
'''

import sys
import random
import jageplatform
import Configuration

config = Configuration.Configuration
config.prefix = str(random.randint(0, 1000000))
config.outfile = config.prefix + 'result.csv'

driver = jageplatform.Driver(config)

changingParameters = ['islands_size', 'feature_chanceToMutate', 'feature_mutationRange']

params = []
for i in range(0, len(changingParameters)):
    params.append((changingParameters[i], sys.argv[i + 1]))
    
driver.setup()
driver.prepare_parameters(params)
driver.run()

print(open(config.execpath + '/' + config.outfile).read())

driver.clean()
