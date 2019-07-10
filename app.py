from tkinter import Tk, Menu, Canvas, NW
from tkinter.filedialog import askopenfilename
from video_loader import VideoLoader
from PIL import Image, ImageTk

class App:
    def donothing(self):
        pass

    def open_file(self):
        filename = askopenfilename()
        self.video = VideoLoader.load_from_file(filename)
        self.canvas = Canvas(self.root, width=self.video.width, height=self.video.height)
        self.display_current_frame()

    def display_current_frame(self):
        if not self.canvas or not self.video.has_next:
            return
        self.canvas.image = ImageTk.PhotoImage(image=Image.fromarray(self.video.current_frame))
        self.canvas.create_image(0, 0, image=self.canvas.image, anchor=NW)
        self.canvas.pack()
        self.root.after(25, self.display_current_frame)

    def next_frame(self):
        self.video.advance()

    def play(self):
        self.next_frame()
        self.root.after(25, self.play)

    def __init__(self):
        self.root = Tk()
        self.canvas = None
        menubar = Menu(self.root)
        
        filemenu = Menu(menubar, tearoff=0)
        filemenu.add_command(label="New", command=self.donothing)
        filemenu.add_command(label="Open", command=self.open_file)
        filemenu.add_command(label="Save", command=self.next_frame)
        filemenu.add_separator()
        filemenu.add_command(label="Exit", command=self.root.quit)
        menubar.add_cascade(label="File", menu=filemenu)

        helpmenu = Menu(menubar, tearoff=0)
        helpmenu.add_command(label="Help Index", command=self.play)
        helpmenu.add_command(label="About...", command=self.donothing)
        menubar.add_cascade(label="Help", menu=helpmenu)

        self.root.config(menu=menubar)
        self.display_current_frame()
        self.root.mainloop()

App()