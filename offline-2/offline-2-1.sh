#!/usr/bin/bash

nodes=(20 40 60 80 100)
flows=(10 20 30 40 50)
packet_rates=(100 200 300 400 500)
coverages=(1 2 3 4 5)

rm -r "offline-2-1-output"
mkdir "offline-2-1-output"

node_data_file_name="offline-2-1-output/node.dat"

for node in ${nodes[@]}
do
    echo "Working on count-stations $node"

    output=$(./ns3 run --quiet "scratch/offline-2-1.cc --count-stations=$node")

    echo "$node $output" >> "$node_data_file_name"
done

node_vs_throughput_plot_file_name="offline-2-1-output/node-vs-throughput.plt"
node_vs_ratio_plot_file_name="offline-2-1-output/node-vs-ratio.plt"

echo "set terminal png size 640, 480" >> "$node_vs_throughput_plot_file_name"
echo "set output \"offline-2-1-output/node-vs-throughput.png\"" >> "$node_vs_throughput_plot_file_name"
echo "plot \"$node_data_file_name\" using 1:2 title \"Node vs Throughput\" with linespoints" >> "$node_vs_throughput_plot_file_name"
echo "set terminal png size 640, 480" >> "$node_vs_ratio_plot_file_name"
echo "set output \"offline-2-1-output/node-vs-ratio.png\"" >> "$node_vs_ratio_plot_file_name"
echo "plot \"$node_data_file_name\" using 1:3 title \"Node vs Ratio\" with linespoints" >> "$node_vs_ratio_plot_file_name"
echo "Plots made for nodes"

flow_data_file_name="offline-2-1-output/flow.dat"

for flow in ${flows[@]}
do
    echo "Working on count-flows $flow"

    output=$(./ns3 run --quiet "scratch/offline-2-1.cc --count-flows=$flow")

    echo "$flow $output" >> "$flow_data_file_name"
done

flow_vs_throughput_plot_file_name="offline-2-1-output/flow-vs-throughput.plt"
flow_vs_ratio_plot_file_name="offline-2-1-output/flow-vs-ratio.plt"

echo "set terminal png size 640, 480" >> "$flow_vs_throughput_plot_file_name"
echo "set output \"offline-2-1-output/flow-vs-throughput.png\"" >> "$flow_vs_throughput_plot_file_name"
echo "plot \"$flow_data_file_name\" using 1:2 title \"Flow vs Throughput\" with linespoints" >> "$flow_vs_throughput_plot_file_name"
echo "set terminal png size 640, 480" >> "$flow_vs_ratio_plot_file_name"
echo "set output \"offline-2-1-output/flow-vs-ratio.png\"" >> "$flow_vs_ratio_plot_file_name"
echo "plot \"$flow_data_file_name\" using 1:3 title \"Flow vs Ratio\" with linespoints" >> "$flow_vs_ratio_plot_file_name"
echo "Plots made for flows"

packet_rate_data_file_name="offline-2-1-output/packet-rate.dat"

for packet_rate in ${packet_rates[@]}
do
    echo "Working on packet-rate $packet_rate"

    output=$(./ns3 run --quiet "scratch/offline-2-1.cc --packet-rate=$packet_rate")

    echo "$packet_rate $output" >> "$packet_rate_data_file_name"
done

packet_rate_vs_throughput_plot_file_name="offline-2-1-output/packet-rate-vs-throughput.plt"
packet_rate_vs_ratio_plot_file_name="offline-2-1-output/packet-rate-vs-ratio.plt"

echo "set terminal png size 640, 480" >> "$packet_rate_vs_throughput_plot_file_name"
echo "set output \"offline-2-1-output/packet-rate-vs-throughput.png\"" >> "$packet_rate_vs_throughput_plot_file_name"
echo "plot \"$packet_rate_data_file_name\" using 1:2 title \"Packet Rate vs Throughput\" with linespoints" >> "$packet_rate_vs_throughput_plot_file_name"
echo "set terminal png size 640, 480" >> "$packet_rate_vs_ratio_plot_file_name"
echo "set output \"offline-2-1-output/packet-rate-vs-ratio.png\"" >> "$packet_rate_vs_ratio_plot_file_name"
echo "plot \"$packet_rate_data_file_name\" using 1:3 title \"Packet Rate vs Ratio\" with linespoints" >> "$packet_rate_vs_ratio_plot_file_name"
echo "Plots made for packet rates"

coverage_data_file_name="offline-2-1-output/coverage.dat"

for coverage in ${coverages[@]}
do
    echo "Working on coverage $coverage"

    output=$(./ns3 run --quiet "scratch/offline-2-1.cc --coverage-area-multiplier=$coverage")

    echo "$coverage $output" >> "$coverage_data_file_name"
done

coverage_vs_throughput_plot_file_name="offline-2-1-output/coverage-vs-throughput.plt"
coverage_vs_ratio_plot_file_name="offline-2-1-output/coverage-vs-ratio.plt"

echo "set terminal png size 640, 480" >> "$coverage_vs_throughput_plot_file_name"
echo "set output \"offline-2-1-output/coverage-vs-throughput.png\"" >> "$coverage_vs_throughput_plot_file_name"
echo "plot \"$coverage_data_file_name\" using 1:2 title \"Coverage vs Throughput\" with linespoints" >> "$coverage_vs_throughput_plot_file_name"
echo "set terminal png size 640, 480" >> "$coverage_vs_ratio_plot_file_name"
echo "set output \"offline-2-1-output/coverage-vs-ratio.png\"" >> "$coverage_vs_ratio_plot_file_name"
echo "plot \"$coverage_data_file_name\" using 1:3 title \"Coverage vs Ratio\" with linespoints" >> "$coverage_vs_ratio_plot_file_name"
echo "Plots made for coverage"

gnuplot -c "$node_vs_throughput_plot_file_name"
gnuplot -c "$node_vs_ratio_plot_file_name"
gnuplot -c "$flow_vs_throughput_plot_file_name"
gnuplot -c "$flow_vs_ratio_plot_file_name"
gnuplot -c "$packet_rate_vs_throughput_plot_file_name"
gnuplot -c "$packet_rate_vs_ratio_plot_file_name"
gnuplot -c "$coverage_vs_throughput_plot_file_name"
gnuplot -c "$coverage_vs_ratio_plot_file_name"

echo "Generated plot images"