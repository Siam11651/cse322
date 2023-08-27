set terminal png size 640, 480
set output "out/throughput-vs-data-rate.png"
plot "out/throughput-vs-data-rate.dat" using 1:2 title "New Reno" with lines
plot "out/throughput-vs-data-rate.dat" using 1:3 title "Westwood Plus" with lines