compile:
	javac $$(find . -name '*.java')
clean:
	rm -r *.class
cc:
	make clean; make compile
run:
	java NetworkSimulator test.txt 1 0.1 0.1 10 10 0 1

