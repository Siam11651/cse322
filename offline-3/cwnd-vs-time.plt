set terminal png size 640, 480
set output "out/cwnd-vs-time.png"
plot "out/congestion-window-1.dat" using 1:2 title "New Reno" with lines, "out/congestion-window-2.dat" using 1:2 title "Westwood Plus" with lines