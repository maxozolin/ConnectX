K?=6
M?=7
N?=4 # Number of things to connnect
CPU_1?=connectx.L1.L1 
CPU_2?=connectx.MxLxPlayer.MxLxPlayer
CPU_H?=connectx.LH.LH
R?=3 # Number of rounds when testing automatically
V?=0 # Verbose test output


ifeq ($(V), 1)
	__extraparams=-v
endif

.PHONY: compile
compile:
	javac -cp ".." *.java */*.java

# Play human-computer
# Compiles and then makes two computers play one against the other (CPU_1 against CPU_2)
play_cc: compile
	java -cp ".." connectx.CXGame $(K) $(M) $(N) $(CPU_1) $(CPU_2)

# Play human-computer
# Compiles and then launches the game against bot specified in CPU_1
play_hc: compile
	java -cp ".." connectx.CXGame $(K) $(M) $(N) $(CPU_1)

build_attacher:
	cd LH/attacher && \
	make build


make_attack_jar: build_attacher
	cd LH && \
	javac Launcher.java && \
	jar -m manifest.txt -c -v -f Launcher.jar Launcher\$$DefineTransformer.class Launcher.class 


play_c_hack: compile make_attack_jar
	java -cp ".." connectx.CXGame $(K) $(M) $(N) $(CPU_1) $(CPU_H)

test_cc: compile make_attack_jar
	java -cp ".." connectx.CXPlayerTester $(K) $(M) $(N) $(CPU_1) $(CPU_H) -r $(R) $(__extraparams)

