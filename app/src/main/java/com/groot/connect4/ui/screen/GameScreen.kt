package com.groot.connect4.ui.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.groot.connect4.R
import com.groot.connect4.di.GameConfig
import com.groot.connect4.di.Opponent
import com.groot.connect4.navigation.NavArgWrapperDto
import com.groot.connect4.navigation.Route
import com.groot.connect4.ui.theme.Connect4Theme
import com.groot.connect4.utils.BoardLogic
import com.groot.connect4.utils.ComputerPlayer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun GameScreen(navController: NavHostController) {
    val isComputerOpponent = remember { mutableStateOf(false) }
    val player1Name = remember { mutableStateOf(Opponent.OPPONENT_SELF) }
    val player2Name = remember { mutableStateOf(Opponent.OPPONENT_AI) }
    val selectedColor = remember { mutableStateOf(R.color.red) }
    val player2Color = remember { mutableStateOf(R.color.red) }
    val player1Color = remember { mutableStateOf(R.color.red) }

    val currentPlayer = remember { mutableStateOf(1) }
    val winner = remember { mutableStateOf<Int?>(0) }
    val board = remember { mutableStateOf(Array(6) { Array(7) { 0 } }) }

    val computerPlayer = remember { ComputerPlayer(board, 2) }

    val wrapper = navController.previousBackStackEntry?.savedStateHandle?.get<NavArgWrapperDto>("navArgWrapperDto")


    LaunchedEffect(key1 = Unit) {
        val config = wrapper?.navArgVo as GameConfig

        if (config.player1Color == R.color.red) {

            player2Color.value = R.color.yellow
            player1Color.value = R.color.red
            currentPlayer.value = 1
        } else {
            player2Color.value = R.color.red
            player1Color.value = R.color.yellow
            currentPlayer.value = 2

            player2Name.value
        }

        when (config.firstTurn) {
            Opponent.OPPONENT_PLAYER -> {
                player1Name.value = Opponent.OPPONENT_PLAYER
                player2Name.value = Opponent.OPPONENT_SELF
            }

            Opponent.OPPONENT_SELF -> {
                player1Name.value = Opponent.OPPONENT_SELF
                if (config.isComputer) {
                    player2Name.value = Opponent.OPPONENT_AI
                } else {
                    player2Name.value = Opponent.OPPONENT_PLAYER
                }
            }

            Opponent.OPPONENT_AI -> {
                player1Name.value = Opponent.OPPONENT_AI
                player2Name.value = Opponent.OPPONENT_SELF
                computerPlayer.makeMove() //make move on launch
            }
        }

        selectedColor.value = config.player1Color// set player 1 color to selected as he goes first
        isComputerOpponent.value = config.isComputer
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorPrimaryDark)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .background(colorResource(id = R.color.colorAccent)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Image(
                modifier = Modifier
                    .padding(start = 25.dp)
                    .clickable {
                        navController.navigate(Route.gameConfigScreen)
                    },
                painter = painterResource(id = R.drawable.ic_action_close),
                contentDescription = "close"
            )


            Image(
                modifier = Modifier
                    .padding(end = 25.dp)
                    .clickable {
                        navController.navigate(Route.gameScreen) //todo restart game
                    },
                painter = painterResource(id = R.drawable.ic_action_restart),
                contentDescription = "close"
            )
        }

        Spacer(modifier = Modifier.height(25.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                //first player
                ColorSelectorCircle(player1Color.value, selectedColor)
                Spacer(modifier = Modifier.height(5.dp))
                Text(text = stringResource(id = getPlayerResId(player1Name.value)), color = colorResource(id = R.color.white))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                //second player
                ColorSelectorCircle(player2Color.value, selectedColor)
                Spacer(modifier = Modifier.height(5.dp))
                Text(text = stringResource(id = getPlayerResId(player2Name.value)), color = colorResource(id = R.color.white))
            }

        }

        Spacer(modifier = Modifier.height(25.dp))


        if ((winner.value ?: 0) != 0) {
            // Display the winner
            Text(
                text = "Player ${winner.value} wins!",
                style = MaterialTheme.typography.headlineLarge,
                color = if (winner.value == 1) colorResource(id = R.color.red) else colorResource(id = R.color.yellow)
            )
        }

        Spacer(modifier = Modifier.weight(1f))


        Board(board) { column ->
            if ((winner.value ?: 0) != 0) {
                return@Board
            }

            if (isComputerOpponent.value) {
                if (currentPlayer.value == 1) {
                    dropColor(board, column, currentPlayer.value)
                    winner.value = when (BoardLogic.checkWin(board.value)) {
                        BoardLogic.Outcome.PLAYER1_WINS -> 1
                        BoardLogic.Outcome.PLAYER2_WINS -> 2
                        else -> null
                    }
                    currentPlayer.value = 2
                    selectedColor.value = R.color.yellow
                    GlobalScope.launch {
                        delay(1000L)
                        computerPlayer.makeMove()
                        winner.value = when (BoardLogic.checkWin(board.value)) {
                            BoardLogic.Outcome.PLAYER1_WINS -> 1
                            BoardLogic.Outcome.PLAYER2_WINS -> 2
                            else -> null
                        }
                        currentPlayer.value = 1
                        selectedColor.value = R.color.red
                    }
                    Log.i("Winner", "checkForWin ${winner.value}")
                }
            } else {
                dropColor(board, column, currentPlayer.value)
                winner.value = when (BoardLogic.checkWin(board.value)) {
                    BoardLogic.Outcome.PLAYER1_WINS -> 1
                    BoardLogic.Outcome.PLAYER2_WINS -> 2
                    else -> null
                }
                Log.i("Winner", "checkForWin ${winner.value}")

                if (currentPlayer.value == 1) {
                    currentPlayer.value = 2
                    selectedColor.value = R.color.yellow
                } else {
                    currentPlayer.value = 1
                    selectedColor.value = R.color.red
                }
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

    }
}

// Drop a color into the column
fun dropColor(board: MutableState<Array<Array<Int>>>, column: Int, player: Int) {
    val newBoard = board.value.map { it.copyOf() }.toTypedArray() // Create a new copy of the board
    for (i in newBoard.size - 1 downTo 0) {
        if (newBoard[i][column] == 0) {
            newBoard[i][column] = player
            break
        }
    }
    board.value = newBoard // Update the board state with the new board
}

@Composable
fun Board(board: MutableState<Array<Array<Int>>>, onColumnClick: (Int) -> Unit) {
    val columns = 7
    val rows = 6

    // Create the game board
    for (i in 0 until rows) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(start = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Draw a column
            for (j in 0 until columns) {
                // Draw a column
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .padding(top = 4.dp)
                        .background(
                            if (board.value[i][j] == 1) colorResource(id = R.color.red)// Red
                            else if (board.value[i][j] == 2) colorResource(id = R.color.yellow) // Yellow
                            else Color.White,
                            CircleShape
                        )
                        .clickable {
                            onColumnClick(j)
                        }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    Connect4Theme {
        GameScreen(navController = rememberNavController())
    }
}
