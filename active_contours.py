from colorsys import rgb_to_hsv
from copy import deepcopy
import time

LIMIT = 0.1


class ActiveContours:
    
    def __init__(self, frame, width, height, x1, y1, x2, y2):
        self.current_frame = frame
        self.width = width
        self.height = height
        self.generate_initial_matrix(x1, y1, x2, y2)

    def generate_initial_matrix(self, x1, y1, x2, y2):
        self.contour_matrix = [[0 for y in range(self.height)] for x in range(self.width)]
        total_r, total_g, total_b = 0, 0, 0
        for x in range(x2-x1):
            for y in range(y2-y1):
                pixel = self.current_frame[y + y1, x + x1]
                total_r += pixel[0]
                total_g += pixel[1]
                total_b += pixel[2]
        pixels_in_region_qty = (x2 - x1) * (y2 - y1)  # Ver si esta cantidad es la misma que la que se toma arriba
        avg_r = total_r / pixels_in_region_qty
        avg_g = total_g / pixels_in_region_qty
        avg_b = total_b / pixels_in_region_qty
        self.avg_hsv = rgb_to_hsv(avg_r, avg_g, avg_b)
        for x in range(self.width):
            for y in range(self.height):
                val = 3
                if x > x1 and x < x2 and y > y1 and y < y2:
                    val = -3
                elif (x == x1 or x == x2) and y >= y1 and y <= y2:
                    val = -1
                elif (y == y1 or y == y2) and x >= x1 and x <= x2:
                    val = -1
                elif (x1 - x == 1 or x - x2 == 1) and y >= y1 and y <= y2:
                    val = 1
                elif (y1 - y == 1 or y - y2 == 1) and x >= x1 and x <= x2:
                    val = 1
                self.contour_matrix[x][y] = val
        for i in range(100):
            print (i)
            self.update_contours()
        # updated = True
        # while updated:
            # updated = self.update_contours()

    def update_contours(self):
        print (time.time(), "a")
        updated = False
        new_contour_matrix = [list(x) for x in self.contour_matrix]
        print (time.time(), "b")
        for x in range(1, self.width - 1):
            for y in range(1, self.height - 1):
                if abs(self.contour_matrix[x][y]) == 3:
                    continue
                r, g, b = self.current_frame[y][x]
                hsv = rgb_to_hsv(r, g, b)
                if self.contour_matrix[x][y] == -1 and abs(self.avg_hsv[0] - hsv[0]) > LIMIT:
                    new_contour_matrix[x][y] = 1;
                    updated = True;
                    for i in range(x-1, x+2):
                        for j in range(y-1, y+2):
                            if self.contour_matrix[i][j] == -3:
                                new_contour_matrix[i][j] = -1
                if self.contour_matrix[x][y] == 1 and abs(self.avg_hsv[0] - hsv[0]) <= LIMIT:
                    new_contour_matrix[x][y] = -1;
                    updated = True;
                    for i in range(x-1, x+2):
                        for j in range(y-1, y+2):
                            if self.contour_matrix[i][j] == 3:
                                new_contour_matrix[i][j] = 1
                            elif self.contour_matrix[i][j] == 1:
                                new_contour_matrix[i][j] = -1
                if self.contour_matrix[x][y] == 1:
                    lin_found = False;
                    for i in range(x-1, x+2):
                        for j in range(y-1, y+2):
                            if self.contour_matrix[i][j] == -1:
                                lin_found = True;
                    if not lin_found:
                        new_contour_matrix[x][y] = 3
        print (time.time(), "c")
        for x in range(1, self.width - 1):
            for y in range(1, self.height - 1):
                if new_contour_matrix[x][y] == -1:
                    next_to_lout = False;
                    for i in range(x-1, x+2):
                        for j in range(y-1, y+2):
                            if new_contour_matrix[i][j] == 1:
                                next_to_lout = True;
                    if not next_to_lout:
                        new_contour_matrix[x][y] = -3
        print (time.time(), "d")
        if updated:
            self.contour_matrix = new_contour_matrix
        print (time.time(), "e")
        return updated
