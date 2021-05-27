import matplotlib.pyplot as plt
import json
import sys

import numpy as np


def mean_makespan(bench_obj):
    time_limit = bench_obj["time_limit"]
    runs = bench_obj["runs"]

    sample_points = np.arange(time_limit + 1)
    vals = [1_000_000]

    # Sample points at 1, 2, ..., time_limit seconds
    for x in sample_points[1:]:
        x_in_ns = x * 1_000_000_000
        makespans = []
        for run in runs:
            val_at_x = np.nan
            for update in run["updates"]:
                if update["time_delta"] > x_in_ns:
                    break

                val_at_x = update["makespan"]

            makespans.append(val_at_x)

        vals.append(np.nanmean(makespans))

    return sample_points, vals


if __name__ == "__main__":
    file_name = sys.argv[1]

    with open(file_name, "rb") as f:
        bench_obj = json.load(f)

    instance = bench_obj["instance"]
    time_limit = bench_obj["time_limit"]

    for run in bench_obj["runs"]:
        seed = run["seed"]
        x = []
        y = []

        for pair in run["updates"]:
            x.append(pair["time_delta"] / 1_000_000_000) # in s
            y.append(pair["makespan"])

        # Add additional data points so the line continues till the end
        plt.step(x + [time_limit], y + [y[-1]], where="post", color="lightgrey")

    x, y = mean_makespan(bench_obj)
    plt.plot(x, y, label="Mean makespan")

    plt.xlim((0, time_limit))
    plt.ylim((y[-1] - 5, y[1] + 5))
    plt.xlabel("Time (s)")
    plt.ylabel("Best makespan")
    plt.title(f"Instance: {instance}")
    plt.show()
