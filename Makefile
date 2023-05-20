K?=6
M?=7
N?=4 # Number of things to connnect
CPU_1?=connectx.L1.L1 
CPU_2?=connectx.L2.L2
R?=3 # Number of rounds when testing automatically
V?=0 # Verbose test output


ifeq ($(V), 1)
	__extraparams+= -v
endif

ifeq ($(G), 1)
	__extraparams+= -g
endif

.PHONY: compile
compile:
	javac -cp ".." *.java */*.java

# Play human-computer
# Compiles and then makes two computers play one against the other (CPU_1 against CPU_2)
play_cc: compile
	java -cp ".." connectx.CXGame $(M) $(N) $(K) $(CPU_1) $(CPU_2)

# Play human-computer
# Compiles and then launches the game against bot specified in CPU_1
play_hc: compile
	java -cp ".." connectx.CXGame $(M) $(N) $(K) $(CPU_2)

test_cc: compile
	java -cp ".." connectx.CXPlayerTester $(M) $(N) $(K) $(CPU_1) $(CPU_2) -r $(R) $(__extraparams)
