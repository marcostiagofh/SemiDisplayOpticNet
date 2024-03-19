import os

log_path = "scripts/logs/"
log_file = "failedSimulations.txt"

if not os.path.exists(log_path):
    os.makedirs(log_path)

# open log file for append
failedLog = open(f"{log_path}{log_file}", "a")

def check_sim (sim_path: str) -> bool:
    with open(sim_path, "r") as f:
        if "rotation" in f.read():
            return True

    return False

def ensure_simulations () -> None:
    for subdir, _, files in os.walk("logs/output/"):
        if "sim.txt" in files:
            if check_sim(f"{subdir}/sim.txt"):
                os.remove(f"{subdir}/sim.txt")

            else:
                failedLog.write(f"Failed simulation at {subdir}")

if __name__ == "__main__":
    ensure_simulations()