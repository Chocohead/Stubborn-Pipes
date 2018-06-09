Conductive pipes are very similar to kinesis pipes. There are some differences however:
 They transfer FU not MJ
 They have transfer limits
 They appear to work a tad more reliably
 They manage to render more reliably
 They render a bit more efficiently
 They can be visually updated with a wrench
<new_page/>
Not to say that are by any means perfect, they too suffer from some visual issues and aren't the lightest on network traffic produced.

The power limits are more complicated than one might expect too.
Rather than having hard limits, they have peak limits that often aren't achieved in long runs without pipes of much higher peak capacities.
<new_page/>
<chapter name="Tiering"/>
# Power Input
//
<pipeLink stack="stubborn_pipes:wood_power"/>
<pipeLink stack="stubborn_pipes:diamond_wood_power"/>

# Power Transfer
//
<pipeLink stack="stubborn_pipes:cobblestone_power"/>
<pipeLink stack="stubborn_pipes:stone_power"/>
<pipeLink stack="stubborn_pipes:sandstone_power"/>
<pipeLink stack="stubborn_pipes:quartz_power"/>
<pipeLink stack="stubborn_pipes:gold_power"/>
<pipeLink stack="stubborn_pipes:diamond_power"/>

# Special
//
<pipeLink stack="stubborn_pipes:iron_power"/>
Iron conductive pipes have a variable limit which can be configured using a wrench.  

<new_page/>
<chapter name="Power Limiting"/>
A pipe's peak power limit can be found within the F3 debug screen, which also provides more information about the energy flow in the pipe being looked at. Remember to try wrenching pipes that seem under utilised first, often that will update them to a much more logical state. Wooden and "emerald" pipes are especially prone to this.
Splits will try balance power equally but may sometimes send power backwards depending if the demand infront of the split outstrips the supply. Piping layout can result in big changes of maximum capacity, call it an exciting quirk.
Loops are strongly discouraged as power can easily become trapped within them and it will reduce the overall power capacity along the run as a result. Pipe plugs are very useful for ensuring this doesn't happen.
<new_page/>
<chapter name="Debugging Cubes"/>
The MJ and FU Testing Cubes provide constant MJ/FU power by default producing 1 MJ and 100 FU respectively.
When shift right clicked with a wrench they will switch modes. The input mode turns into an MJ/FU drain which will accept as much power as they're given.
The third mode is the same as the first for the FU Testing Cube, but the MJ Testing Cube will produce the minimum an acceptor allows compared to the first that produces the maximum it allows. BuildCraft doesn't seem to utilise this currently however.

The current mode of a testing cube can be checked by right clicking using a wrench.

As they are purely designed for testing they do not have recipes, and use textures from vanilla blocks.