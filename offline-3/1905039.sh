outdir="out"

rm -r "$outdir"
mkdir "$outdir"

throughput_vs_data_rate_file="throughput-vs-data-rate.dat"

touch "$outdir/$throughput_vs_data_rate_file"
echo "# DataRate Throughput1 Throughput2" >> "$outdir/$throughput_vs_data_rate_file"

for i in $(seq 10 10 300)
do
    average_throughput=$(./ns3 run --quiet "scratch/1905039/1905039.cc --bottleneck-rate=$i")
    echo "$i $average_throughput" >> "$outdir/$throughput_vs_data_rate_file"
done

gnuplot -c "scratch/throughput-vs-data-rate.plt"

throughput_vs_loss_rate_file="throughput-vs-loss-rate.dat"

touch "$outdir/$throughput_vs_loss_rate_file"
echo "# LossRate Throughput1 Throughput2" >> "$outdir/$throughput_vs_loss_rate_file"

for i in $(seq -6 0.1 -1)
do
    average_throughput=$(./ns3 run --quiet "scratch/1905039/1905039.cc --loss-rate-exponent=$i")
    echo "$i $average_throughput" >> "$outdir/$throughput_vs_loss_rate_file"
done

gnuplot -c "scratch/throughput-vs-loss-rate.plt"