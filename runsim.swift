type stringfile;
type plotfile;
type datafile;

app (stringfile out) bash(string command) {
  bash "-c" command stdout=@out;
}

(string out[]) gencross(string sets[][]) {
	string setreg[];	
	foreach set,i in sets {
		setreg[i] = @arg("leftb")+@strjoin(set,",")+@arg("rightb");
	}
	stringfile tmpfile = bash("echo "+@strjoin(setreg,"_"));
	string tmpstr = readData(tmpfile);
	out = @strsplit(tmpstr," ");
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
string samples[] = ["0","1","2"];

int lengths[];
foreach param,i in params {
    lengths[i] = @length(param);
}

string configparams[][];
configparams[0] = params[0];
configparams[1] = params[1];
configparams[2] = samples;

string configs[] = gencross(configparams);
datafile outfiles[];

foreach config, i in configs {
        datafile f<
                regexp_mapper;
                source=config,
                match="(.*)",
                transform="sim_\\1.dat">;
        f = simulation(@strsplit(config,"_"));
        outfiles[i] = f;
}

string avgs[] = gencross(params);

foreach avg in avgs {
        datafile sample_files[]<
                structured_regexp_mapper;
                source=samples,
                match="(.*)",
                transform="sim_"+avg+"\\1_summary.dat">;
        datafile summary<
                regexp_mapper;
                source=avg,
                match="(.*)",
                transform="sim_\\1_summary.dat">;
}



