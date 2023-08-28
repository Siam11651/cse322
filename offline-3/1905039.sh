outdir="out"

rm -rf "$outdir"
mkdir "$outdir"

for algorithm in $(echo "TcpNewReno TcpHighSpeed TcpAdaptiveReno")
do
    throughput_vs_data_rate_file="throughput-vs-data-rate-${algorithm,,}.dat"
    throughput_vs_loss_rate_file="throughput-vs-loss-rate-${algorithm,,}.dat"
    data_rate_plot_file="throughput-vs-data-rate-${algorithm,,}.png"
    loss_rate_plot_file="throughput-vs-loss-rate-${algorithm,,}.png"
    cwnd_data1_file="congestion-window-1-${algorithm,,}.dat"
    cwnd_data2_file="congestion-window-2-${algorithm,,}.dat"
    cwnd_plot_file="cwnd-vs-time-${algorithm,,}.png"

    touch "$outdir/$throughput_vs_data_rate_file"
    echo "# DataRate Throughput1 Throughput2" >> "$outdir/$throughput_vs_data_rate_file"

    for i in $(seq 10 10 300)
    do
        average_throughput=$(./ns3 run --quiet "scratch/1905039/1905039.cc --algorithm="$algorithm" --bottleneck-rate=$i")
        echo "$i $average_throughput" >> "$outdir/$throughput_vs_data_rate_file"
    done

    gnuplot -c "scratch/throughput.plt" "$algorithm" "TcpWestwoodPlus" "$outdir/$throughput_vs_data_rate_file" "$outdir/$data_rate_plot_file"

    touch "$outdir/$throughput_vs_loss_rate_file"
    echo "# LossRate Throughput1 Throughput2" >> "$outdir/$throughput_vs_loss_rate_file"

    for i in $(seq -6 0.1 -2)
    do
        average_throughput=$(./ns3 run --quiet "scratch/1905039/1905039.cc --algorithm="$algorithm" --loss-rate-exponent=$i")
        echo "$i $average_throughput" >> "$outdir/$throughput_vs_loss_rate_file"
    done

    gnuplot -c "scratch/throughput.plt" "$algorithm" "TcpWestwoodPlus" "$outdir/$throughput_vs_loss_rate_file" "$outdir/$loss_rate_plot_file"

    ./ns3 run --quiet "scratch/1905039/1905039.cc --algorithm="$algorithm" --trace-congestion" > /dev/null

    gnuplot -c "scratch/cwnd-vs-time.plt" "$algorithm" "TcpWestwoodPlus" "$outdir/$cwnd_data1_file" "$outdir/$cwnd_data2_file" "$outdir/$cwnd_plot_file"
done
