import os
import matplotlib.pyplot as plt
import numpy as np
from plot_gameplay_results import heatmap, annotate_heatmap
import math


def load_file(game_id, params):
    file_name = f"Game_{game_id}_{'_'.join(params)}.txt"
    results = np.zeros((20, 3))
    if os.path.exists(file_name):
        with open(file_name, "r") as f:
            for line in f.readlines():
                if "Mean Score" in line:
                    elements = line.split(";")
                    idx = int(elements[0])
                    score = float(elements[1])
                    ticks = float(elements[2].split(":")[1])
                    game_won = True if elements[3].split(":")[1] == "PLAYER_WINS" else False
                    results[idx-1, 0] = score
                    results[idx-1, 1] = ticks
                    results[idx-1, 2] = game_won
    else:
        return (0, 0, 10000, 10000)
    return (np.mean(results[:, 2]), #win-rate
            np.mean(results[:, 0]), #mean score
            np.mean(results[:, 1][results[:, 2] == 1]), # avg. ticks won
            np.mean(results[:, 1][results[:, 2] == 0])) # avg. ticks lost

def store_file(game_id, data_list, parameter_settings):
    with open(f"data/Botdata_{game_id}.csv", "w") as f:
        f.write("Algorithm ; Parameter 1; Parameter 2; Parameter 3; Win-Rate ; Score; Ticks-won; Ticks-Lost\n")
        for data, params in zip(data_list, parameter_settings):
            x = params + list(data)
            f.write(" ; ".join([str(val) for val in x]) + "\n")


game_names = ["aliens", "boulderdash", "butterflies", "chase", "frogs",
              "missilecommand", "portal", "sokoban", "survive zombies", "zelda"]
game_ids = [0, 11, 13, 18, 39, 55, 63, 75, 79, 90]
parameter_settings = [["BFS", "100", "false", "false"],
                      ["BFS", "100", "false", "true"],
                      ["BFS", "100", "true", "false"],
                      ["BFS", "100", "true", "true"],
                      ["BFS", "200", "false", "false"],
                      ["BFS", "200", "false", "true"],
                      ["BFS", "200", "true", "false"],
                      ["BFS", "200", "true", "true"],
                      ["MCTS", "10", "5", "false"],
                      ["MCTS", "10", "5", "true"],
                      ["MCTS", "10", "10", "false"],
                      ["MCTS", "10", "10", "true"],
                      ["MCTS", "20", "5", "false"],
                      ["MCTS", "20", "5", "true"],
                      ["MCTS", "20", "10", "false"],
                      ["MCTS", "20", "10", "true"],
                     ]

ranks = [[0] * 16, [0] * 16, [0] * 16, [0] * 16, [0] * 16, [0] * 16, [0] * 16, [0] * 16,
         [0] * 16, [0] * 16, [0] * 16, [0] * 16, [0] * 16, [0] * 16, [0] * 16, [0] * 16]
points = [0] * 16
game_results = np.zeros((10, 16))
formula = {0: 25, 1: 18, 2: 15, 3: 12, 4: 10, 5: 8, 6: 6, 7: 4, 8: 2, 9: 1, 10: 0, 11: 0, 12: 0, 13: 0, 14:0, 15:0}


all_results = dict()
for (game_idx, game_id), game in zip(enumerate(game_ids), game_names):
    all_results[game_id] = []
    for parameter_setting in parameter_settings:
        results = load_file(game_id, parameter_setting)
        all_results[game_id].append(results)

    store_file(game_id, all_results[game_id], parameter_settings)


    res = [[idx] + [round(y, 2) for y in all_results[game_id][idx]] for idx, _ in enumerate(all_results[game_id])]
    for ind, x in enumerate(res):
        if math.isnan(x[3]):
            res[ind][3] = 10000
        if math.isnan(x[4]):
            res[ind][4] = 10000

    o = sorted(range(len(res)), key=lambda k: (res[k][1], res[k][2], -res[k][3], res[k][4]), reverse=True)
    rank = [0] * 16
    current_rank = 0
    skip = 1
    for io in range(len(o) - 1):
        rank[o[io]] = current_rank
        if res[o[io]][1:15] != res[o[io + 1]][1:15]:
            current_rank += skip
            skip = 1
        else:
            skip += 1
    rank[o[-1]] = current_rank

    # print(game, [res[x][0] for x in o])

    for io, x in enumerate(o):
        ##for p, x in zip([5, 4, 3, 2, 1], o):
        # print(points, res[x][0])
        points[res[x][0]] += formula[rank[o[io]]]

    for j, x in enumerate(o):
        # print(i, x)
        ranks[res[x][0]][rank[o[j]]] += 1
        game_results[game_idx, res[x][0]] = rank[o[j]]


import matplotlib.pyplot as plt
import matplotlib.font_manager as font_manager

#plt.rc('pgf', texsystem='lualatex')
#plt.rc('text', usetex=True)
#plt.rc('text.latex',
#       preamble=r'\usepackage{amsmath}\usepackage{kmath}\usepackage{kerkis}\renewcommand\sfdefault\rmdefault')
#font_path = '/home/alex/fonts/kerkis.ttf'
#prop = font_manager.FontProperties(fname=font_path)

grid = dict(height_ratios=[5], width_ratios=[15])
fig, ax = plt.subplots(ncols=1, nrows=1, gridspec_kw=grid)
fig.set_figheight(15)
fig.set_figwidth(13)
im, cbar = heatmap(game_results.transpose() + 1,
                   ["+".join(x) for x in parameter_settings],
                   game_names, ax=ax, vmin=1, vmax=16, cbar=False, cmap=plt.cm.get_cmap('Blues_r', 16),
                   cbar_kw={"ticks": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]})
annotate_heatmap(im, game_results.transpose() + 1, valfmt="{x:1.0f}", threshold=1, threshold2=3)
plt.tight_layout()
plt.savefig("figures/agent-ranks.pdf")
plt.savefig("figures/agent-ranks.png")
plt.show()
