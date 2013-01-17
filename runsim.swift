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

(int out[]) countlengths(string ps[][]) {
	foreach param,i in ps {
	    out[i] = @length(param);
	}
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

(string out[][]) dimdec(string sets[][], int dim) {
	foreach set,i in sets {
		if(i<dim) {
			out[i] = set;
		}
		if(i>dim) {
			out[i-1] = set;
		}
	}
}

(projectindex out[]) project(string sets[][], int dim, int lengths[]) {
	string decsets[][] = dimdec(sets,dim);	
	string indexsets[][];	
	foreach decset,i in decsets {
		indexsets[i] = indexlist(decset);
	}
	string summaries[][] = gencross(indexsets);	
	
	foreach summary,i in summaries {		
		string newsets[][];
		foreach param,j in summary {
			if(j<dim) {
				newsets[j][0] = param;
				out[i].params[j] = sets[j][@toint(param)];
			}
			if(j>=dim) {
				newsets[j+1][0] = param;
				out[i].params[j+1] = sets[j+1][@toint(param)];
			}
		}
		foreach set,j in sets[dim] {
			newsets[dim][j] = @strcat(j);
		}
		out[i].params[dim]  = "x";
		string indset[][] = gencross(newsets);
		foreach inds,k in indset {
			out[i].indices[k] = findindex(inds,lengths);
		}
	}
}

app (datafile out) simulation(string args[]) {
	runner args stdout=@filename(out);
}

app (plotfile out) plot(datafile data) {
    plotter @filename(data) @filename(out);   
}

app (datafile out) avgcount(datafile files[]) {
	summarize @filenames(files) stdout=@filename(out);
}

app (plotfile out) plotagg(datafile files[]) {
	aggplotter @filename(out) @filenames(files);
}

string params[][];
params[0] = ["5","10","15"];
params[1] = ["0.2","0.4"];
params[2] = ["0.1","0.2"];
# samples
params[3] = ["0","1","2","3"];

int lens[] = countlengths(params);
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
datafile summaryfiles[];
foreach avg,i in avgs {
        datafile summary<
                regexp_mapper;
                source=@strjoin(avg.params,"_"),
                match="(.*)",
                transform="sim_\\1_summary.dat">;
	datafile samplefiles[];
	foreach index,j in avg.indices {
		samplefiles[j] = outfiles[index];
	}
	summary = avgcount(samplefiles);
    summaryfiles[i] = summary;
    plotfile pf <regexp_mapper;
                source=@filename(summary),
                match="(.*)dat",
                transform="\\1png">;

    pf = plot(summary);
}

string summaryparams[][] = dimdec(params,3);
int summarylens[] = countlengths(summaryparams);
foreach param,pi in summaryparams {
	projectindex aggs[] = project(summaryparams,pi,summarylens);
	foreach agg,i in aggs {
		datafile aggfile<
		        regexp_mapper;
		        source=@strjoin(agg.params,"_"),
		        match="(.*)",
		        transform="sim_\\1_agg.dat">;
		datafile sampleaggfiles[];
		foreach index,j in agg.indices {
			sampleaggfiles[j] = summaryfiles[index];
		}
		plotfile plotf<
		        regexp_mapper;
		        source=@strjoin(agg.params,"_"),
		        match="(.*)",
		        transform="sim_\\1_agg.png">;
		plotf = plotagg(sampleaggfiles);		
	}
}


