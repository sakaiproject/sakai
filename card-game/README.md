# S2U-42 Sakai Card Game (Name That Face)

A small game available in the roster tool, with the purpose of learning student names.
The instructor will be shown images of students and need to guess the name of them.

The card-game API that can be found within this directory provides the CardGameService and
the CardGameStatItem entity.

The game is written in JavaScript and placed in the roster tool. It interfaces with the backend
through the REST API (CardGameController), which can be found in the webapi folder.

```
           __________________________       _______________________
           |                        |       |                     |
           |   StatItemRepository   |------>|   CardGameService   |---+
           |   ./card-game          |<------|   ./card-game       |<+ |
           |________________________|       |_____________________| | |
_____________________________________                               | |
|                                   |                               | |
| Roster tool (Handlebars)          |                               | |
| ./roster                          |                               | |
|   ______________________________  |       ________________________|_v__________
|   |                            |  |       |                                   |
|   |   card-game (JavaScript)   |--------->|   CardGameController (REST API)   |
|   |   ./roster/**/js/card-game |<---------|   ./webapi                        |
|   |____________________________|  |       |___________________________________|
|___________________________________|

```
