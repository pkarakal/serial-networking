from pandas import read_csv
from matplotlib import pyplot
import glob
import cv2
import sys


def parse_echo_csv():
    all_files = glob.glob("echo*.csv")
    for filename in all_files:
        frame = read_csv(filename, header=0, index_col=0, usecols=[0, 3])
        frame.plot()
    pyplot.show()


def parse_ack_csv():
    all_files = glob.glob("ack_echo*.csv")
    for filename in all_files:
        fig = pyplot.figure()
        frame = read_csv(filename, header=0, index_col=0, usecols=[0, 3, 4])
        pyplot.plot(frame["Duration"])
        pyplot.xlabel("Packets")
        pyplot.ylabel("Duration (s)")
        fig.text(.5, .05, f"Bit Rate Error (BER): {calculate_ber(frame)}", ha='center')
    pyplot.show()


def calculate_ber(frame):
    total_packets = len(frame)
    res = frame.groupby("Time sent").count()
    p = 0
    for i in res.Duration.keys().tolist():
        index = res.Duration.keys().tolist().index(i)
        p += res["Duration"].values[index] if i > 1 else 0
    print(p)
    return p/total_packets


def open_images():
    all_images = glob.glob("image*.jpg")
    for image in all_images:
        img = cv2.imread(image)
        if img is None:
            sys.exit("Could not read the image.")
        cv2.imshow(image, img)
        cv2.waitKey(0)
        cv2.destroyAllWindows()


if __name__ == "__main__":
    open_images()
    parse_echo_csv()
    parse_ack_csv()
