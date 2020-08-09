# Sample Commander

Sample Commander is an application designed to assist in organizing audio files, also known as [samples.](https://en.wikipedia.org/wiki/Sampling_(music))

It is written & available as an Eclipse IDE plugin but it will soon be available as a standalone e4 RCP application.

### 💡 Motivation

Sample Commander aims to offer the convenient sample browsing experience provided in modern [DAW](https://en.wikipedia.org/wiki/Digital_audio_workstation)'s with an [orthodox file manager](https://en.wikipedia.org/wiki/File_manager#Orthodox_file_managers) interface.

It takes inspiration from software such as:

- **[FL Studio](https://www.image-line.com/flstudio/)'s [file browser](https://www.image-line.com/support/flstudio_online_manual/html/browser.htm):** provides a single file tree to view the user's sample library. It is convenient for finding sounds to use in a song, but limited in its ability to quickly reorganize the sample library's structure.

- **[Krusader](https://krusader.org/):** A Qt based orthodox file manager. Provides excellent file mangement capabilities and workflow. 

## Features:
- ⌨️ Navigate file directory's quickly with your keyboard

- 🗄️ View multiple directory contents side-by-side

- 📂 Supports common file operations (cut, copy, delete, etc.) between directories. 

- 🎵 Audio playback for .wav, .mp3 and .flac is supported and can be started by selecting a supported file.  

- <img src="https://img.icons8.com/color/48/000000/git.png" width="24" height ="24"> Git integration to keep track of changes to your audio library

## 📸 Screenshots:

![how_it_looks_1.png](./images/how_it_looks_2.png)

## 📥 Installation

### Update Site

`https://raw.githubusercontent.com/AObuchow/Sample-Commander/updatesite/updatesite/`

## 📚 Libraries used:

- [java-stream-player](https://github.com/goxr3plus/java-stream-player) is used for audio playback

- [push-p2-repo-action](https://github.com/ingomohr/push-p2-repo-action) provides a GitHub CI Action used to build and release the project

- [egit](https://github.com/eclipse/egit) provides git integration

- [Eclipse Spectrum Theme](https://github.com/AObuchow/Eclipse-Spectrum-Theme) is used to style the application
