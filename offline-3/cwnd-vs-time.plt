set terminal png size 640, 480
set output ARG5
plot ARG3 using 1:2 title ARG1 with lines, ARG4 using 1:2 title ARG2 with lines