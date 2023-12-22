
# CodeAssist v3: Reimagined & Refocused

CodeAssist v3 is back, completely rebuilt from the ground up! We've ditched the complexities of Gradle and switched to **a custom build system**,
tailor-made for a smoother, more efficient development experience. While this may mean that AGP features and Gradle plugins are not going to
be supported, core functionalities will be implemented.

But this isn't just about technical upgrades. We're making a bold move: **CodeAssist v3 is no longer chasing Android Studio**.
Instead, we're laser-focused on empowering aspiring coders with a dedicated IDE designed for simplicity and intuitiveness.


* **Effortless Setup**: Ditch the configuration woes! Our custom build system takes the complexity out of getting started.
* **Clear and Concise UI**: No confusing menus or overwhelming options. We prioritize a clean interface that guides you through every step.
* **Enhanced Performance**: Experience smooth editing, fast compilation, and efficient resource management.
* **Interactive Learning Tools**: Built-in tutorials, code completion, and error explanations pave the way for a learning-focused experience.
* **Improved Java Support**: While still using javac, I have gained significant knowledge on how the internals of the 
compiler work. Which can be leveraged for faster syntax analysis.
* **Customizable Workspace**: Tailor your coding environment with themes, plugins, and keyboard shortcuts.

### CodeAssist v3 isn't just an IDE, it's a launchpad for your coding journey. We're building an accessible and enjoyable environment where anyone can take their first steps into the world of programming.

### We believe CodeAssist v3 has the potential to be the ultimate mobile IDE for developers. Join us on this journey and help us build something truly amazing!

### FAQs:

* **Why ditch Gradle?** Adding gradle was never really the purpose of CodeAssist. It's heavy and requires users 
to download the whole Android SDK, Java SDK which makes it difficult for beginner on-ramp. It also
runs unsigned code which is unsafe. 
* **When will CodeAssist v3 be stable and released?** We're currently in active development, and we'll announce a stable release date once core features are complete and thoroughly tested.
* **Will CodeAssist v3 be compatible with my existing CodeAssist projects?** Since we're moving to a new build system, no. Old projects won't be compatible.
* **Will CodeAssist v3 remain open-source?** Absolutely! We believe in the power of open-source and will continue to develop CodeAssist v3 as a community-driven project.

### Project Structure Info 
- **javac:** The custom build of javac based on netbeans.
- **project:** Contains CodeAssist's project model, responsible for parsing projects,
building the project dependency graph, and configuration
- **completions:** Responsible for handling java parsing, syntax analysis, and code completions.
- **compiler:** The custom build system, responsible for resolving the dependency graph and running
the actual compilation of projects.
- **desktop-test:** Test version used to quickly prototype on desktop and test features.