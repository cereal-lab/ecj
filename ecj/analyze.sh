#!/bin/bash
cd target/classes
for i in {1..2};
do 
    echo "Running test $i";
    java ec.Evolve -file ec/app/multiplexerslow/11.params
    cat out.stat | grep -oP '(?<=Standardized=)(.*?)(?=\s)' | paste -sd "," - >> ../../res.csv
    echo "Done running $i";
done;