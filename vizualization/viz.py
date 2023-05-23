import matplotlib.pyplot as plt
import numpy as np


x_arr = np.array([0])
y_arr = np.array([0])
with open('/tmp/game_tmp_log','r') as f:
  lines = f.readlines()
  count = 0;
  for line in lines:
    x_arr = np.append(x_arr, [int(line.strip())])
    y_arr = np.append(y_arr, [count * 2])
    count +=1

print(x_arr)
plt.plot(x_arr, y_arr)
plt.show()

