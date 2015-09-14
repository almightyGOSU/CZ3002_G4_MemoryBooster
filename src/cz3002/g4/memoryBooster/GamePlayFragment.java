package cz3002.g4.memoryBooster;

import java.util.ArrayList;

import cz3002.g4.util.Const;
import cz3002.g4.util.LayoutUtil;
import cz3002.g4.util.Const.GameMode;
import cz3002.g4.util.Const.UserStatus;
import cz3002.g4.util.FacebookDataSource;
import cz3002.g4.util.StringUtil;
import cz3002.g4.util.TimeUtil;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GamePlayFragment extends FragmentActivity {
	
	private UserStatus _userStatus = null;
	private GameMode _gameMode = null;
	
	// UI Elements
	private TextView _tv_gameTimeText = null;
	private TextView _tv_gameTime = null;
	private ImageView _iv_questionImage = null;
	private LinearLayout _ll_userChoices = null;
	private Button _btn_option1 = null;
	private Button _btn_option2 = null;
	private Button _btn_option3 = null;
	private Button _btn_option4 = null;
	private TextView _tv_numCorrect = null;
	private TextView _tv_numWrong = null;
	
	private ProgressDialog _pd_gameStatus = null;
	
	// FacebookDataSource
    private FacebookDataSource _fbDataSrc = null;
    
    // Gameplay
    private boolean _bGameOver = false;
    private ArrayList<Question> _questionSet = null;
    private Question _currQuestion = null;
    private int _currQuestionNum = 0;
    private int _numQuestions = 0;
    private int _numCorrect = 0;
    private int _numWrong = 0;
    
    // Timers
    private CountDownTimer _cdTimer = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameplay_frag);
        
        Intent intent = getIntent();
        _userStatus = (UserStatus) intent.getSerializableExtra(Const.USER_STATUS);
        _gameMode = (GameMode) intent.getSerializableExtra(Const.GAME_MODE);
        
		Log.d("GamePlayFragment", _userStatus.toString());
		Log.d("GamePlayFragment", _gameMode.toString());
		
		getUIElements();
		setUpInteractiveElements();
		setUpTimeText();
		
		// Testing using 'Timed Challenge' mode
		_numQuestions = Const.TIMED_CHALLENGE_QUESTIONS;
		new GenerateQuestionsTask().execute(String.valueOf(_numQuestions));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    
    /** Getting UI Elements from layout */
	private void getUIElements() {
		
		_tv_gameTimeText = (TextView) findViewById(R.id.tv_gameTimeText);
		_tv_gameTime = (TextView) findViewById(R.id.tv_gameTime);
		
		_iv_questionImage = (ImageView) findViewById(R.id.iv_questionImage);
		
		_ll_userChoices = (LinearLayout) findViewById(R.id.ll_userChoices);
		_btn_option1 = (Button) findViewById(R.id.btn_option1);
		_btn_option2 = (Button) findViewById(R.id.btn_option2);
		_btn_option3 = (Button) findViewById(R.id.btn_option3);
		_btn_option4 = (Button) findViewById(R.id.btn_option4);
		
		_tv_numCorrect = (TextView) findViewById(R.id.tv_numCorrect);
		_tv_numWrong = (TextView) findViewById(R.id.tv_numWrong);
	}
	
	/** Set up interactive UI Elements */
	private void setUpInteractiveElements() {
		
		Button.OnClickListener btnOnClickListener = new Button.OnClickListener() {
			public void onClick(View view) {
				
				// Disable all buttons
				LayoutUtil.setClickable(_ll_userChoices, false);

				// Process chosen option
				final Button btn = (Button) view;
				String userChoice = (String) btn.getText();
				
				// Check answer, update button
				boolean bCorrect = checkAnswer(userChoice);
				btn.setBackgroundResource(
						bCorrect ? R.drawable.btn_correct :
							R.drawable.btn_wrong);
				
				// Get next question after slight delay of 750ms
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						
						if(!_bGameOver) {
							
							btn.setBackgroundResource(R.drawable.btn_user_choice);
							
							// Enable all buttons
							LayoutUtil.setClickable(_ll_userChoices, true);
							getNextQuestion();
						}
					}
				}, 750);
			}
		};
		
		_btn_option1.setOnClickListener(btnOnClickListener);
		_btn_option2.setOnClickListener(btnOnClickListener);
		_btn_option3.setOnClickListener(btnOnClickListener);
		_btn_option4.setOnClickListener(btnOnClickListener);
	}
	
	/** Set up the time text based on chosen game mode */
	private void setUpTimeText() {
		
		switch(_gameMode) {
		case CAMPAIGN_MODE:
			_tv_gameTimeText.setText(R.string.elapsed_time);
			break;
		case FF:
			_tv_gameTimeText.setText(R.string.elapsed_time);
			break;
		case TIMED_CHALLENGE:
			_tv_gameTimeText.setText(R.string.time_left);
			break;
		default:
			break;
		}
	}
	
	/** Checks user choice against answer */
	private boolean checkAnswer(String userChoice) {
		
		boolean bCorrect = _currQuestion.checkAnswer(userChoice);
		if(bCorrect) {
			
			_numCorrect++;
			_tv_numCorrect.setText(String.valueOf(_numCorrect));
		}
		else {
			
			_numWrong++;
			_tv_numWrong.setText(String.valueOf(_numWrong));
		}
		
		return bCorrect;
	}
	
	/** Gets the next question! */
	private void getNextQuestion() {
		
		// Timed Challenge - to be fixed
		// To be used to safeguard against all generated questions
		// answered before timer runs out
		if(_currQuestionNum >= _numQuestions) {
			
			// Disable all buttons
			LayoutUtil.setClickable(_ll_userChoices, false);
			return;
		}
		
		_currQuestion = _questionSet.get(_currQuestionNum);
		
		_iv_questionImage.setImageBitmap(_currQuestion.getQuestionImage());
		ArrayList<String> questionOptions = _currQuestion.getQuestionOptions();
		_btn_option1.setText(questionOptions.get(0));
		_btn_option2.setText(questionOptions.get(1));
		_btn_option3.setText(questionOptions.get(2));
		_btn_option4.setText(questionOptions.get(3));
		
		_currQuestionNum++;
		if(_currQuestionNum >= _numQuestions) {
			
			// Should trigger some score calculation
			// Saving score, progress to database
			// Saving score to global highscore table
			Log.d("GamePlayFragment", "GAME OVER!!");
		}
	}
	
	private void startNewTimer(long millisInFuture, long countDownInterval) {
		
		if(_cdTimer != null) {
			_cdTimer.cancel();
			_cdTimer = null;
		}
		
		// Reset countdown text
		_tv_gameTime.setText(TimeUtil.timeToString(
				TimeUtil.millisecondsToSeconds(millisInFuture)));
		
		// Create new countdown timer
		_cdTimer = new CountDownTimer(millisInFuture, countDownInterval) {

			public void onTick(long millisUntilFinished) {
				
				_tv_gameTime.setText(TimeUtil.timeToString(
						TimeUtil.millisecondsToSeconds(millisUntilFinished)));
			}

			public void onFinish() {
				_tv_gameTime.setText(TimeUtil.timeToString(0));
				
				// Trigger game over stuff
				Log.d("Timed Challenge", "GAME OVER!");
				
				_bGameOver = true;
				
				// Disable all buttons
				LayoutUtil.setClickable(_ll_userChoices, false);
				
				Toast.makeText(getApplicationContext(),
						"Game Over! Calculating highscore..\n\n"
						+ "Going back to main menu, for now :)",
						Toast.LENGTH_LONG).show();
				
				// Go back main menu after 3000 ms
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						
						// Go back main menu, for now
						Intent intent = new Intent(
								getApplicationContext(), MainFragment.class);
						intent.putExtra(Const.BACK_TO_MAIN, true);
						startActivity(intent);
					}
				}, 3000);
			}
		};
		
		_cdTimer.start();
	}
	
	private class GenerateQuestionsTask extends AsyncTask<String, Void, String> {
		
		protected void onPreExecute() {
			
			Log.d("GenerateQuestionsTask", "I am here in onPreExecute!");
			
			_pd_gameStatus = new ProgressDialog(GamePlayFragment.this);
			_pd_gameStatus.setIndeterminate(true);
			_pd_gameStatus.setMessage(
					StringUtil.enlargeString("Generating questions.."));
			_pd_gameStatus.setCancelable(false);
			_pd_gameStatus.setCanceledOnTouchOutside(false);
			_pd_gameStatus.show();
		}
		
		// Generate questions
		// params[0]: Number of questions to generate
		protected String doInBackground(String... params) {
			
			Log.d("GenerateQuestionsTask", "I am here in doInBackground!");
			
			int numQuestions = Integer.parseInt(params[0]);
			_fbDataSrc = new FacebookDataSource(getApplicationContext());
			
			// Generate questions
			_questionSet = QuestionGenerator.generateFbQuestions(
					_fbDataSrc, numQuestions);
			
			Log.d("GenerateQuestionsTask",
					"# of Questions: " + _questionSet.size());
			
			// TEMPORARY fix to prevent app crashing when user
			// has too little facebook friends?
			// Lesser than 4 is still a problem obviously..
			// Not enough friends to generate 'choices' for questions
			if(_questionSet.size() < _numQuestions) {
				
				_numQuestions = _questionSet.size();
			}
			
			// Gets the first question (Hidden behind dialog at this time)
			runOnUiThread(new Runnable() {
				public void run() {
					
					// Timed Challenge - Reset countdown text
					_tv_gameTime.setText(TimeUtil.timeToString(
									Const.TIMED_CHALLENGE_DURATION));
					
					// For getting the first question
					getNextQuestion();
				}
			});
			
			try {	
				for(int i = 3; i > 0; i--) {
					
					final int time = i;
					runOnUiThread(new Runnable() {
						public void run() {
							_pd_gameStatus.setMessage(
									StringUtil.enlargeString(
											"Game starting in " + time + ".."));
						}
					});
					Thread.sleep(900);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			return "Success";
		}

		protected void onPostExecute(String result) {
			
			Log.d("GenerateQuestionsTask", "I am here in onPostExecute!");
			
			_pd_gameStatus.dismiss();
			
			// Reset game status
			_bGameOver = false;
			
			// Start a new Timer
			startNewTimer(TimeUtil.secondsToMilliseconds(
					Const.TIMED_CHALLENGE_DURATION), 1000);
			return;
		}
	}
}
