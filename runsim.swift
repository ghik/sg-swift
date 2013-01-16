type stringfile;
type plotfile;
type datafile;

app (stringfile out) bash(string command) {
  bash "-c" command stdout=@out;
}

(string out[][]) gencross(string sets[][]) {
	string setreg[];	
	foreach set,i in sets {
		setreg[i] = @arg("leftb")+@strjoin(set,",")+@arg("rightb");
	}
	stringfile tmpfile = bash("echo "+@strjoin(setreg,"_"));
	string tmpstr = readData(tmpfile);
	string[] lines = @strsplit(tmpstr," ");
	foreach line,i in lines {
		out[i] = @strsplit(line,"_");
	}
}

(string out[][]) project(string sets[][], int dim) {
	string setsdim[][];	
	foreach set,i in sets {
		if(i<dim) {
			setsdim[i] = set;
		}
		if(i>dim) {
			setsdim[i-1] = set;
		}
	}
	out = gencross(setsdim);	
}

(int out[]) findindexes(string sets[], string set[], int dim) {	
}


app (datafile out) simulation(string args[]) {
	echo args stdout=@filename(out);
}

app (plotfile out) plot(datafile data) {
    echo stdin=@filename(data) stdout=@filename(out);   
}

app (datafile out) avgcount(datafile files[]) {
	echo @filenames(files) stdout=@filename(out);
}

string params[][];
params[0] = ["5","10"];
params[1] = ["0.2","0.4"];
# samples
params[2] = ["0","1","2"];

int lengths[];
foreach param,i in params {
    lengths[i] = @length(param);
}

string configs[][] = gencross(params);
trace(configs);

datafile outfiles[];
foreach config,i in configs {
        datafile f<
                regexp_mapper;
                source=@strjoin(config,"_"),
                match="(.*)",
                transform="sim_\\1.dat">;
        f = simulation(config);
        outfiles[i] = f;
}

string avgs[][] = project(params,2);

foreach avg in avgs {
	int sampleindexes[] = findindexes(avg,params[2],2);
	trace(sampleindexes);
        datafile summary<
                regexp_mapper;
                source=avg,
                match="(.*)",
                transform="sim_\\1_summary.dat">;
#	summary = avgcount(samplefiles);	
}