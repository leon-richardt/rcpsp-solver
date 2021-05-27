# Running

## Benchmark script
The benchmark script must be run from the repository's root directory with:
```bash
python scripts/run_benchmark.py <path to instance> <time limit> <replications>
```
The result is saved in the root directory as a JSON file with the file name `<instance name>.bench`.
Each replication is ran with a different seed.
The benchmark file will only be written if no errors occur during the runs.


## Visualization script
The visualization script is run with:
```bash
python scripts/plot_improvements.py <path to benchmark file>
```
