import cv2
from video import Video

class VideoLoader:
    @staticmethod
    def load_from_file(filename):
        cap = cv2.VideoCapture(filename)
        return Video(cap)
