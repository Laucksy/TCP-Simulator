compile:
	javac $$(find . -name '*.java')
clean:
	rm -r *.class
cc:
	make clean; make compile
run:
	java NetworkSimulator test.txt 1 0.1 0.1 10 10 0 1
single:
	java NetworkSimulator ${type}.txt 1 0.5 0.5 50 10 1 1 100 30
nolc:
	java NetworkSimulator ${type}.txt 1 0 0 50 10 0 1
