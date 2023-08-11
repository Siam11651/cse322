#!/usr/bin/bash

nodes=(20 40 60 80 100)
flows=(10 20 30 40 50)
packet_rates=(100 200 300 400 500)
speeds=(5 10 15 20 25)

rm -r "offline-2-2-output"
mkdir "offline-2-2-output"

node_data_file_name="offline-2-2-output/node.dat"

for node in ${nodes[@]}
do
    echo "Working on count-stations $node"

    output=$(./ns3 run --quiet "scratch/1905039_2.cc --count-stations=$node")

    echo "$node $output" >> "$node_data_file_name"
done

node_vs_throughput_plot_file_name="offline-2-2-output/node-vs-throughput.plt"
node_vs_ratio_plot_file_name="offline-2-2-output/node-vs-ratio.plt"

echo "set terminal png size 640, 480" >> "$node_vs_throughput_plot_file_name"
echo "set output \"offline-2-2-output/node-vs-throughput.png\"" >> "$node_vs_throughput_plot_file_name"
echo "plot \"$node_data_file_name\" using 1:2 title \"Node vs Throughput\" with linespoints" >> "$node_vs_throughput_plot_file_name"
echo "set terminal png size 640, 480" >> "$node_vs_ratio_plot_file_name"
echo "set output \"offline-2-2-output/node-vs-ratio.png\"" >> "$node_vs_ratio_plot_file_name"
echo "plot \"$node_data_file_name\" using 1:3 title \"Node vs Ratio\" with linespoints" >> "$node_vs_ratio_plot_file_name"
echo "Plots made for nodes"

flow_data_file_name="offline-2-2-output/flow.dat"

for flow in ${flows[@]}
do
    echo "Working on count-flows $flow"

    output=$(./ns3 run --quiet "scratch/1905039_2.cc --count-flows=$flow")

    echo "$flow $output" >> "$flow_data_file_name"
done

flow_vs_throughput_plot_file_name="offline-2-2-output/flow-vs-throughput.plt"
flow_vs_ratio_plot_file_name="offline-2-2-output/flow-vs-ratio.plt"

echo "set terminal png size 640, 480" >> "$flow_vs_throughput_plot_file_name"
echo "set output \"offline-2-2-output/flow-vs-throughput.png\"" >> "$flow_vs_throughput_plot_file_name"
echo "plot \"$flow_data_file_name\" using 1:2 title \"Flow vs Throughput\" with linespoints" >> "$flow_vs_throughput_plot_file_name"
echo "set terminal png size 640, 480" >> "$flow_vs_ratio_plot_file_name"
echo "set output \"offline-2-2-output/flow-vs-ratio.png\"" >> "$flow_vs_ratio_plot_file_name"
echo "plot \"$flow_data_file_name\" using 1:3 title \"Flow vs Ratio\" with linespoints" >> "$flow_vs_ratio_plot_file_name"
echo "Plots made for flows"

packet_rate_data_file_name="offline-2-2-output/packet-rate.dat"

for packet_rate in ${packet_rates[@]}
do
    echo "Working on packet-rate $packet_rate"

    output=$(./ns3 run --quiet "scratch/1905039_2.cc --packet-rate=$packet_rate")

    echo "$packet_rate $output" >> "$packet_rate_data_file_name"
done

packet_rate_vs_throughput_plot_file_name="offline-2-2-output/packet-rate-vs-throughput.plt"
packet_rate_vs_ratio_plot_file_name="offline-2-2-output/packet-rate-vs-ratio.plt"

echo "set terminal png size 640, 480" >> "$packet_rate_vs_throughput_plot_file_name"
echo "set output \"offline-2-2-output/packet-rate-vs-throughput.png\"" >> "$packet_rate_vs_throughput_plot_file_name"
echo "plot \"$packet_rate_data_file_name\" using 1:2 title \"Packet Rate vs Throughput\" with linespoints" >> "$packet_rate_vs_throughput_plot_file_name"
echo "set terminal png size 640, 480" >> "$packet_rate_vs_ratio_plot_file_name"
echo "set output \"offline-2-2-output/packet-rate-vs-ratio.png\"" >> "$packet_rate_vs_ratio_plot_file_name"
echo "plot \"$packet_rate_data_file_name\" using 1:3 title \"Packet Rate vs Ratio\" with linespoints" >> "$packet_rate_vs_ratio_plot_file_name"
echo "Plots made for packet rates"

speed_data_file_name="offline-2-2-output/speed.dat"

for speed in ${speeds[@]}
do
    echo "Working on speed $speed"

    output=$(./ns3 run --quiet "scratch/1905039_2.cc --speed=$speed")

    echo "$speed $output" >> "$speed_data_file_name"
done

speed_vs_throughput_plot_file_name="offline-2-2-output/speed-vs-throughput.plt"
speed_vs_ratio_plot_file_name="offline-2-2-output/speed-vs-ratio.plt"

echo "set terminal png size 640, 480" >> "$speed_vs_throughput_plot_file_name"
echo "set output \"offline-2-2-output/speed-vs-throughput.png\"" >> "$speed_vs_throughput_plot_file_name"
echo "plot \"$speed_data_file_name\" using 1:2 title \"Speed vs Throughput\" with linespoints" >> "$speed_vs_throughput_plot_file_name"
echo "set terminal png size 640, 480" >> "$speed_vs_ratio_plot_file_name"
echo "set output \"offline-2-2-output/speed-vs-ratio.png\"" >> "$speed_vs_ratio_plot_file_name"
echo "plot \"$speed_data_file_name\" using 1:3 title \"Speed vs Ratio\" with linespoints" >> "$speed_vs_ratio_plot_file_name"
echo "Plots made for speed"

gnuplot -c "$node_vs_throughput_plot_file_name"
gnuplot -c "$node_vs_ratio_plot_file_name"
gnuplot -c "$flow_vs_throughput_plot_file_name"
gnuplot -c "$flow_vs_ratio_plot_file_name"
gnuplot -c "$packet_rate_vs_throughput_plot_file_name"
gnuplot -c "$packet_rate_vs_ratio_plot_file_name"
gnuplot -c "$speed_vs_throughput_plot_file_name"
gnuplot -c "$speed_vs_ratio_plot_file_name"

echo "Generated plot images"