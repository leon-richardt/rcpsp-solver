import argparse
import itertools
import json
import subprocess

from string import Template
from pathlib import Path


class Config:
    flags_template = Template(
        "--crossover=$crossover --mutation-probability=$mutprob "
        "--no-improvement-threshold=$threshold "
        "--parent-selection=$parentselection --cache-makespans=$cache")

    def __init__(self, crossover, mutation_probability,
                 no_improvement_threshold, parent_selection, cache_makespans):
        self.crossover = crossover
        self.mutation_probability = mutation_probability
        self.no_improvement_threshold = no_improvement_threshold
        self.parent_selection = parent_selection
        self.cache_makespans = cache_makespans

    @classmethod
    def fromother(cls, other):
        return cls(other.crossover, other.mutation_probability,
                   other.no_improvement_threshold, other.parent_selection,
                   other.cache_makespans)

    def to_cli_flags(self):
        return Config.flags_template.substitute(
            crossover=self.crossover,
            mutprob=self.mutation_probability,
            threshold=self.no_improvement_threshold,
            parentselection=self.parent_selection,
            cache=self.cache_makespans)


def get_all_configs():
    BASE_CONFIG = Config("one-point", "0.5", "0", "fixed-size", "false")

    crossovers = ["two-point"]
    mutation_probs = ["0", "0.25", "0.75", "1"]
    no_improvement_thresholds = ["10", "100", "1000"]
    parent_selections = ["random-size"]
    cache_makespans = ["true"]

    all_configs = []
    all_configs.append(Config.fromother(BASE_CONFIG))

    for arg in crossovers:
        conf = Config.fromother(BASE_CONFIG)
        conf.crossover = arg
        all_configs.append(conf)

    for arg in mutation_probs:
        conf = Config.fromother(BASE_CONFIG)
        conf.mutation_probability = arg
        all_configs.append(conf)

    for arg in no_improvement_thresholds:
        conf = Config.fromother(BASE_CONFIG)
        conf.no_improvement_threshold = arg
        all_configs.append(conf)

    for arg in parent_selections:
        conf = Config.fromother(BASE_CONFIG)
        conf.parent_selection = arg
        all_configs.append(conf)

    for arg in cache_makespans:
        conf = Config.fromother(BASE_CONFIG)
        conf.cache_makespans = arg
        all_configs.append(conf)

    return all_configs


def get_benchmark_files():
    path = "benchmark_instances/"
    instances = [
        "X6_8.RCP",
        "X9_6.RCP",
        "X10_9.RCP",
        "X27_6.RCP",
        "X30_8.RCP",
        "X42_7.RCP",
        "X51_6.RCP",
        "X55_10.RCP",
        "X58_3.RCP",
        "X59_3.RCP",
    ]

    return [Path(path + instance) for instance in instances]


def get_all_combinations():
    configs = get_all_configs()
    instances = get_benchmark_files()

    return [comb for comb in itertools.product(configs, instances)]


def get_seeds():
    seeds = [
        "85838798",
        "84183543",
        "9459607",
        "40594335",
        "59528639",
        "8948803",
        "61045676",
        "55911072",
        "41299189",
        "81907177"
    ]

    return seeds


def build_benchmark_file_name(instance_name, config):
    name = f"{instance_name}-{config.crossover}-{config.mutation_probability}-" \
           f"{config.no_improvement_threshold}-{config.parent_selection}-" \
           f"{config.cache_makespans}"

    return name


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("solution_path",
                        type=Path,
                        help="Path to save solution files to")
    parser.add_argument("benchmark_path",
                        type=Path,
                        help="Path to save benchmark files to")
    parser.add_argument("--skip",
                        type=int,
                        default=0,
                        help="Number of combinations to skip at the beginning")

    return parser.parse_args()


if __name__ == "__main__":
    REPLICATIONS = 10
    COMBS = get_all_combinations()
    INSTANCES = get_benchmark_files()
    SEEDS = get_seeds()
    TIME_LIMIT = 60

    args = parse_args()

    cmd_template = Template("java -jar target/rcp-solver-0.1.0.jar $instance "
                            "$solutionpath $limit $seed --should-log=true")

    for comb_idx in range(args.skip, len(COMBS)):
        config, instance_path = COMBS[comb_idx]
        print(f"Running combination {comb_idx + 1}/{len(COMBS)} ...")
        save_stem = build_benchmark_file_name(instance_path.name, config)

        flags = config.to_cli_flags()

        result_obj = {}
        result_obj["instance"] = instance_path.name
        result_obj["time_limit"] = TIME_LIMIT
        result_obj["replications"] = len(SEEDS)
        result_obj["runs"] = []

        for i, seed in enumerate(SEEDS):
            print(f"    Running seed {i + 1}/{len(SEEDS)} ...",
                  end=" ",
                  flush=True)

            solution_path = args.solution_path.joinpath(f"{save_stem}-{seed}.sol")
            cmd = cmd_template.substitute(instance=str(instance_path),
                                          limit=TIME_LIMIT,
                                          seed=seed,
                                          solutionpath=str(solution_path))
            cmd += " " + flags
            proc = subprocess.run(cmd,
                                  capture_output=True,
                                  text=True,
                                  shell=True)

            if proc.returncode != 0:
                print("\nSolver run failed with an error:", proc.stderr)
                continue

            update_lines = proc.stdout.splitlines()[:-1]

            run_obj = {"seed": seed}
            run_obj["makespan_updates"] = []
            run_obj["member_updates"] = []

            # check for prefixes
            for line in update_lines:
                if line.startswith("delta: "):
                    _, time_delta, iteration, makespan = line.split()

                    run_obj["makespan_updates"].append({
                        "time_delta": int(time_delta),
                        "iteration": int(iteration),
                        "makespan": int(makespan)
                    })

                elif line.startswith("member: "):
                    _, time_delta, iteration = line.split()

                    run_obj["member_updates"].append({
                        "time_delta": int(time_delta),
                        "iteration": int(iteration)
                    })

                elif line.startswith("iterations: "):
                    _, iterations = line.split()
                    run_obj["iterations"] = int(iterations)

            result_obj["runs"].append(run_obj)
            print("done.")

        bench_path = args.benchmark_path.joinpath(save_stem + ".bench")
        with open(bench_path, "w") as result_file:
            json.dump(result_obj, result_file)
