package venus.glue.js.terminal

open class Command(
    val name: String,
    val execute: (parsedInput: MutableList<String>, t: Terminal, sudo: Boolean) -> String =
            { a, b, c -> throw NotImplementedError() },
    val tab: (parsedInput: MutableList<String>, t: Terminal, sudo: Boolean) -> MutableList<Any> =
            { a, b, c -> throw NotImplementedError() },
    val help: String = "Command does not have a help yet!"
) {
    companion object {
        private val allCommands = arrayListOf<Command>()

        fun getCommands(): ArrayList<String> {
            val cmds = ArrayList<String>()
            for (cmd in allCommands) {
                cmds.add(cmd.name)
            }
            return cmds
        }

        operator fun get(name: String) =
                allCommands.firstOrNull { it.name == name }
                        ?: throw CommandNotFoundError(name)
    }

    init {
        allCommands.add(this)
    }

    override fun toString() = name
}