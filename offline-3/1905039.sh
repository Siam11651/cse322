outdir="out"

rm -rf "$outdir"
mkdir "$outdir"

for algorithm in $(echo "TcpWestwoodPlus TcpHighSpeed TcpAdaptiveReno")
do
    data_rate_file="data-rate-${algorithm,,}.dat"
    loss_rate_file="loss-rate-${algorithm,,}.dat"
    data_rate_throughput_plot_file="throughput-vs-data-rate-${algorithm,,}.png"
    loss_rate_throughput_plot_file="throughput-vs-loss-rate-${algorithm,,}.png"
    data_rate_fairness_plot_file="fairness-vs-data-rate-${algorithm,,}.png"
    loss_rate_fairness_plot_file="fairness-vs-loss-rate-${algorithm,,}.png"
    cwnd_data1_file="congestion-window-1-${algorithm,,}.dat"
    cwnd_data2_file="congestion-window-2-${algorithm,,}.dat"
    cwnd_plot_file="cwnd-vs-time-${algorithm,,}.png"

    touch "$outdir/$throughput_vs_data_rate_file"
    echo "# DataRate Throughput1 Throughput2 Fairness" >> "$outdir/$data_rate_file"

    for i in $(seq 50 50 300)
    do
        data=$(./ns3 run --quiet "scratch/1905039/1905039.cc --algorithm="$algorithm" --bottleneck-rate=$i")
        echo "$i $data" >> "$outdir/$data_rate_file"
    done

    gnuplot -c "scratch/throughput.plt" "TcpNewReno" "$algorithm" "$outdir/$data_rate_file" "$outdir/$data_rate_throughput_plot_file" "Data Rate (Mbps)"
    gnuplot -c "scratch/fairness.plt" "$outdir/$data_rate_file" "$outdir/$data_rate_fairness_plot_file" "Data Rate (Mbps)"

    touch "$outdir/$loss_rate_file"
    echo "# LossRate Throughput1 Throughput2 Fairness" >> "$outdir/$loss_rate_file"

    for i in $(seq -6 0.5 -2)
    do
        data=$(./ns3 run --quiet "scratch/1905039/1905039.cc --algorithm="$algorithm" --loss-rate-exponent=$i")
        echo "$i $data" >> "$outdir/$loss_rate_file"
    done

    gnuplot -c "scratch/throughput.plt" "TcpNewReno" "$algorithm" "$outdir/$loss_rate_file" "$outdir/$loss_rate_throughput_plot_file" "Loss Rate Exponent"
    gnuplot -c "scratch/fairness.plt" "$outdir/$loss_rate_file" "$outdir/$loss_rate_fairness_plot_file" "Loss Rate Exponent"

    ./ns3 run --quiet "scratch/1905039/1905039.cc --algorithm="$algorithm" --trace-congestion" > /dev/null

    gnuplot -c "scratch/cwnd-vs-time.plt" "TcpNewReno" "$algorithm" "$outdir/$cwnd_data1_file" "$outdir/$cwnd_data2_file" "$outdir/$cwnd_plot_file"
done
