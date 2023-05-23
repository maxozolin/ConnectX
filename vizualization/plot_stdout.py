import sys

def main():
    while True:
        try:
            data = sys.stdin.readline()
            sys.stdout.write(data)
            sys.stdout.flush()
        except KeyboardInterrupt:
            print('exiting')
            sys.exit()
main()
