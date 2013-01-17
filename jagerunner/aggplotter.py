import sys
import os
import subprocess
import random

gpl_template = """

set ylabel 'fitness'
set xlabel 'iteration'
set xrange [-1:]

set datafile separator ','

set terminal pngcairo
set output "{output}"
plot {inputs}

"""

gpl_input_template = '"{input}" using 1:2 with lines'

outfile = sys.argv[1]
infiles = sys.argv[2:]

plotfile = str(random.randint(0, 1000000)) + '.gpl'

plots = ', '.join(map(lambda f: gpl_input_template.format(input=f), infiles))

with open(plotfile, 'w') as pf:
    pf.write(gpl_template.format(inputs=plots, output=outfile))

with open(plotfile) as pf:
    subprocess.check_call(['gnuplot'], stdin=pf)

os.remove(plotfile)

