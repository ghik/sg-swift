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
plot "{input}" using 1:2 with lines

"""

infile = sys.argv[1]
outfile = sys.argv[2]

plotfile = str(random.randint(0, 1000000)) + '.gpl'

with open(plotfile, 'w') as pf:
    pf.write(gpl_template.format(input=infile, output=outfile))

with open(plotfile) as pf:
    subprocess.check_call(['gnuplot'], stdin=pf)

os.remove(plotfile)

