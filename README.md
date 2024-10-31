# Setup

## Build docker image
```bash
docker build -t my-spinalhdl-dev:latest -f ./.devcontainer/Dockerfile-spinalhdl .
```

## Open in Dev Containers
- Install docker
- Install [Dev Containers extension of vscode](https://code.visualstudio.com/docs/devcontainers/tutorial)
- Open this folder in vscode
- Press `F1` -> type `reopen in container`

## Import the project to enable code hint and jump
- In vscode, press `F1` -> type `import build`

## Project structure
- `build.sc`: config file of package manager [mill](https://mill-build.org/mill/0.10.15/Intro_to_Mill.html)
- `examples/src/`: source code

# How to run?

## examples/src/01Simulation.scala: Hello world
```bash
mill ex.runMain sim.HelloWorld
```
- `ex` is the project name in `./build.sc`
- `sim` is the package name in the first line of `examples/src/01Simulation.scala`

## examples/src/01Simulation.scala: SimpleComponent
```bash
mill ex.runMain sim.SimpleComponent
```
- The line "SpinalVerilog{Top()}" will generate a Verilog file of this component.

## Other examples
For every `object xxx extends App` in package `yyy`, use
```bash
mill ex.runMain yyy.xxx
```
to run `xxx`.

# More examples
- [SpinalWorkshop](https://github.com/SpinalHDL/SpinalWorkshop/tree/workshop/src/main/scala/workshop)

# More resource
- Foreword and Introduction in [SpinalHDL Doc](https://spinalhdl.github.io/SpinalDoc-RTD/master/index.html)
- [Scala Guide](https://spinalhdl.github.io/SpinalDoc-RTD/master/SpinalHDL/Getting%20Started/Scala%20Guide/index.html)
- [Hands-on Scala](https://www.handsonscala.com/table-of-contents.html)
- [Help for VHDL people](https://spinalhdl.github.io/SpinalDoc-RTD/master/SpinalHDL/Getting%20Started/Help%20for%20VHDL%20people/index.html)
- [The VexRiscV CPU - A New Way to Design](https://tomverbeure.github.io/rtl/2018/12/06/The-VexRiscV-CPU-A-New-Way-To-Design.html)
- Next version of VexRiscv, which uses the same design pattern but a different API: [VexiiRiscv](https://spinalhdl.github.io/VexiiRiscv-RTD/master/VexiiRiscv/Introduction/index.html)
