type stringfile;
type plotfile;
type datafile;

type projectindex {
	string params[];
 	int indices[];
}

app (stringfile out) bash(string command) {
  bash "-c" command stdout=@out;
}

(string out[][]) gencross(string sets[][]) {
	string setreg[];	
	foreach set,i in sets {
		if(@length(set)>1) {
			setreg[i] = @arg("leftb")+@strjoin(set,",")+@arg("rightb");
		} else {
			setreg[i] = @strjoin(set,",");
		}
	}
	stringfile tmpfile = bash("echo "+@strjoin(setreg,"_"));
	string tmpstr = readData(tmpfile);
	string[] lines = @strsplit(tmpstr," ");
	foreach line,i in lines {
		out[i] = @strsplit(line,"_");
	}
}

(string out[]) indexlist(string set[]) {
	foreach elem,i in set {
		out[i]=@strcat(i);
	}
}

(int out) findindex(string indices[], int lengths[]) {
	int tmparray[];
	tmparray[0] = 0;
	foreach index,i in indices {
		tmparray[i+1] = tmparray[i]*lengths[i] + @toint(indices[i]);
	}
	out = tmparray[@length(indices)];
}

(projectindex out[]) project(string sets[][], int dim, int lengths[]) {
	string indexsets[][];	
	foreach set,i in sets {
		if(i<dim) {
			indexsets[i] = indexlist(set);
		}
		if(i>dim) {
			indexsets[i-1] = indexlist(set);
		}
	}
	string summaries[][] = gencross(indexsets);	
	
	foreach summary,i in summaries {		
		string newsets[][];
		foreach param,j in summary {
			if(j<dim) {
				newsets[j][0] = param;
				out[i].params[j] = sets[j][@toint(param)];
			}
			if(j>dim) {
				newsets[j+1][0] = param;
				out[i].params[j] = sets[j+1][@toint(param)];
			}
		}
		newsets[dim] = sets[dim];
		string indset[][] = gencross(newsets);
		foreach inds,k in indset {
			out[i].indices[k] = findindex(inds,lengths);
		}
	}
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
params[0] = ["5","10","15"];
params[1] = ["0.2","0.4"];
params[2] = ["0.1","0.2"];
# samples
params[3] = ["0","1","2","3"];

int lens[];
foreach param,i in params {
    lens[i] = @length(param);
}

string configs[][] = gencross(params);

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


projectindex avgs[] = project(params,3,lens);

foreach avg in avgs {
        datafile summary<
                regexp_mapper;
                source=@strjoin(avg.params,"_"),
                match="(.*)",
                transform="sim_\\1_summary.dat">;
	datafile samplefiles[];
	foreach index,i in avg.indices {
		samplefiles[i] = outfiles[index];
	}
	summary = avgcount(samplefiles);	
}
