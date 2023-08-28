set terminal png size 640, 480
set output ARG3
set xlabel ARG4
set ylabel "Fairness"
plot ARG2 using 1:4 title ARG1 with lines