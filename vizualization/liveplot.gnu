set datafile separator ',';
plot "/tmp/plotcache" using 1:2 with lines
pause 1
reread
