CACHEFILE='/tmp/plotcache'

POSITIONAL_ARGS=()
while [[ $# -gt 0 ]]; do
  case $1 in
    -m|--extension)
      M_ARG="M=$2"
      shift # past argument
      shift # past value
      ;;
    -k|--extension)
      K_ARG="K=$2"
      shift # past argument
      shift # past value
      ;;
    -n|--searchpath)
      N_ARG="N=$2"
      shift # past argument
      shift # past value
      ;;
    -r|--rounds)
      ROUNDS_ARG="R=$2"
      shift # past argument
      shift # past value
      ;;
    -v|--verbose)
      VERBOSE_ARG="V=1"
      shift # past argument
      ;;
    -t|--timestep)
      TIMESTEP="$2"
      shift # past argument
      shift # past value
      ;;
    -*|--*)
      echo "Unknown option $1"
      exit 1
      ;;
    *)
      POSITIONAL_ARGS+=("$1") # save positional arg
      shift # past argument
      ;;
  esac
done

set -- "${POSITIONAL_ARGS[@]}" # restore positional parameters

echo "M               = ${M_ARG}"
echo "N               = ${N_ARG}"
echo "K               = ${K_ARG}"
echo "TIME            = ${TIMESTEP}"
echo "ROUNDS          = ${ROUNDS_ARG}"
echo "VERBOSE         = ${VERBOSE_ARG}"

#START_TEST_CMD="make test_cc ${M_ARG} ${N_ARG} ${K_ARG} ${ROUNDS_ARG} ${VERBOSE_ARG} 2>&1" ;
function start_test(){
  make test_cc ${M_ARG} ${N_ARG} ${K_ARG} ${ROUNDS_ARG} ${VERBOSE_ARG} CPU_1=connectx.MxLxPlayer.MxLxPlayer 2>&1
}

function player_counter_termeter(){
  awk 'BEGIN{OFS="\t";st=systime(); lastT=0;print "moves"} /\(MxLxPlayer\) \->/{++cnt;currT=systime()-st;if(currT!=lastT){print cnt;lastT=currT;fflush()}}' < /dev/stdin > /dev/stdout
}

function disp_termeter(){
  start_test | player_counter | termeter -t ll
}

PLAYER_EXTRACTOR_CMD="awk 'BEGIN{st=systime(); lastT=0;print \"time\\tmoves\"} /\(MxLxPlayer\) \->/{++cnt;currT=systime()-st;if(currT!=lastT){print currT,\"\\t\",cnt;lastT=currT}}'"
#PYTHON_PLOTTER_CMD='python3 -u vizualization/plot_stdout.py'
#bash -c "$START_TEST_CMD | $PLAYER_EXTRACTOR_CMD | $PYTHON_PLOTTER_CMD"
#echo "$START_TEST_CMD";
#bash -c "$START_TEST_CMD | $PLAYER_EXTRACTOR_CMD"
#bash -c "$START_TEST_CMD | $PLAYER_EXTRACTOR_CMD |termeter -t ll"
#bash -c "$START_TEST_CMD | $PLAYER_EXTRACTOR_CMD | tr"

function player_counter(){
  awk 'BEGIN{OFS=",";st=systime(); lastT=0;} /\(MxLxPlayer\) \->/{++cnt;currT=systime()-st;if(currT!=lastT){print currT,cnt;lastT=currT;fflush()}}' < /dev/stdin > /dev/stdout
}


function player_counter_graph(){
  awk 'BEGIN{OFS=",";st=systime(); lastT=0;} /\(MxLxPlayer\) \->/{++cnt;currT=systime()-st;if(currT!=lastT){print cnt;lastT=currT;fflush()}}' < /dev/stdin > /dev/stdout
}

function local_gnuplot(){
  #gnuplot -p -e """
  #set datafile separator ',';
  #plot '${CACHEFILE}' using 1:2 with lines
  #pause 1
  #reread
  #"""
  gnuplot ./vizualization/liveplot.gnu
}

#start_test | player_counter |gnuplot -p -e "set datafile separator \",\";plot '-' using 1:2 with lines;"
#start_test | player_counter #|gnuplot -p -e "set datafile separator \",\";plot '-' using 1:2 with lines;"

#start_test | player_counter >${CACHEFILE} & 
start_test
#sleep 5
#local_gnuplot
