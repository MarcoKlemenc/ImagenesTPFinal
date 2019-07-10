import cv2

class Video:
    def __init__(self, cap):
        self.cap = cap
        self.width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
        self.height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
        self.has_next = True
        self.advance()
    
    def advance(self):
        if self.has_next:
            self.has_next, self.current_frame = self.cap.read()
            if not self.has_next:
                self.cap.release()
            return True
        return False
