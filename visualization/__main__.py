from pandas import read_csv
from matplotlib import pyplot
import glob
import cv2
import sys


def parse_echo_csv():
    all_files = glob.glob("echo*.csv")
    for filename in all_files:
        fig = pyplot.figure()
        frame = read_csv(filename, header=0, index_col=0, usecols=[0, 3])
        pyplot.plot(frame["Duration"])
        pyplot.xlabel("Packets")
        pyplot.ylabel("Duration")
        fig.text(.5, .05, f"Mean response time: {calculate_mean_response_time(frame)}", ha='center')
    pyplot.show()


def parse_ack_csv():
    all_files = glob.glob("ack_echo*.csv")
    for filename in all_files:
        fig = pyplot.figure()
        frame = read_csv(filename, header=0, index_col=0, usecols=[0, 3, 4])
        # plot_times_sent(frame)
        pyplot.plot(frame["Duration"])
        pyplot.xlabel("Packets")
        pyplot.ylabel("Duration (s)")
        fig.text(.5, .05, f"Bit Rate Error (BER): {calculate_ber(frame)}", ha='center')
    pyplot.show()


def calculate_ber(frame):
    ack = len(frame)
    res = frame.groupby("Time sent").count()
    nack = 0
    for key, value in enumerate(res.Duration.keys()):
        nack += res["Duration"].values[key] * value if value > 1 else 0
    return 1.0 - (float(ack) / float(ack + nack)) ** (1.0/128.0)


def calculate_mean_response_time(frame):
    return frame["Duration"].mean()


def plot_times_sent(frame):
    frame.groupby("Time sent")["Time sent"].hist(bins=10)


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
