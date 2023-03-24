K?=6
M?=7
N?=4
CPU_1?=connectx.L0.L0
CPU_2?=connectx.L1.L1

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

