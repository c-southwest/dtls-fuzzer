import sys
import os
import os.path
import subprocess
import argparse

# simple script for launching experiments

JAR_LOCATION="TLS-ModelBasedTester.jar"

COMMAND_TEMPLATE="java -cp \"{}:{}/*\"  se.uu.it.modeltester.Main @{} "

# a launcher for experiments
# takes as arguments the arguments file and optionally the jar location
def print_usage():
    print("Usage: python launcher.py args_file [dtls-tool.jar][args for java]")

 

def launch_experiment(arg_file, jar_file, lib_dir, redir, other_args = []):
    command = COMMAND_TEMPLATE.format(jar_file, lib_dir, arg_file)
    command += " ".join(other_args)
    print("Launching ", command)
    if redir:
        redir_out, redir_err=sys.stdout, sys.stderr
    else:
        redir_out, redir_err=subprocess.DEVNULL, subprocess.DEVNULL
    subp = subprocess.Popen(command, shell=True, 
        stdout=redir_out, 
        stderr=redir_err, 
        close_fds=True)
    print("Process pid: ", subp.pid)

def launch_experiments(args_dir, jar_file, redir, other_args = []):
    lib_dir = os.path.join(os.path.dirname(jar_file), "lib")
    for arg_file in os.listdir(args_dir):
        if os.path.isfile:
            launch_experiment(os.path.join(args_dir,arg_file), jar_file, lib_dir, redir, other_args=other_args)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Utility for launching multiple instances of DTLS learner')
    parser.add_argument('-a', "--args", required=True, type=str, help="directory with argument files/argument file")
    parser.add_argument('-r', "--redirect", action='store_true', help="redirect process output/error to stdout/stderr")
    parser.add_argument('-j', "--jar", required=False, type=str, default=JAR_LOCATION, help="DTLS learner .jar file, note that a lib dir should be in the same directory as the .jar ")
    parser.add_argument('-p', "--params", required=False, nargs='+', default=[], help="additional parameters to pass to the learning processes (which augment/overwrite those in the argument files)")
    args = parser.parse_args()
    print(args.params)
    launch_experiments(args.args, args.jar, args.redirect, other_args=args.params)