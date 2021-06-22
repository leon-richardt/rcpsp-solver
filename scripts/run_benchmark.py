import argparse
import json
import subprocess

from string import Template
from pathlib import Path

def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("instance", type=str, help="Path to the instance that "
                        "should be benchmarked")
    parser.add_argument("limit", type=int, help="Time limit for each "
                        "individual run (in seconds)")
    parser.add_argument("replications", type=int, help="Number of times to "
                        "repeat the measurement")

    return parser.parse_args()


if __name__ == "__main__":
    args = parse_args()

    cmd_template = Template("java -jar target/rcp-solver-0.1.0.jar $instance "
                            "sol.txt $limit $seed --should-log=true")

    instance_path = Path(args.instance)

    result_obj = {}
    result_obj["instance"] = instance_path.name
    result_obj["time_limit"] = args.limit
    result_obj["replications"] = args.replications
    result_obj["runs"] = []

    for i in range(args.replications):
        print(f"Running replication {i + 1}/{args.replications} ...", end=" ", flush=True)

        cmd = cmd_template.substitute(instance=args.instance, limit=args.limit,
                                      seed=i)
        proc = subprocess.run(cmd, capture_output=True, text=True, check=True, shell=True)
        update_lines = proc.stdout.splitlines()[:-1]

        run_obj = {"seed": i}
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

    with open(instance_path.name + ".bench", "w") as result_file:
        json.dump(result_obj, result_file)
