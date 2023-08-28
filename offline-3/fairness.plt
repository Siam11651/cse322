set terminal png size 640, 480
set output ARG2
set xlabel ARG3
set ylabel "Fairness"
plot ARG1 using 1:4 title "Fairness" with lines