import sys

filenames = sys.argv[1:]
filecount = len(filenames)

avgs = []

for filename in filenames:
    f = open(filename)
    for line in f:
        line = line.strip()
        if len(line) == 0:
            continue
        spl = line.split(',')
        step = int(spl[0].strip())
        value = float(spl[1].strip())
        part = value / filecount
        if len(avgs) <= step:
            avgs.append(part)
        else:
            avgs[step] += part
    f.close()
        
output = ""
for step in range(0, len(avgs)):
    output += "{},{}\n".format(step, avgs[step])

print(output)
