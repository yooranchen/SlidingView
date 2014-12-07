package yooranchen.org.slidingview;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;


public class MainActivity extends ActionBarActivity {

    private SlidingView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new SlidingView(this);
        view.setLeftOffSet(0.8f);//设置菜单大小
        setContentView(view);
        view.setLeftMenuView(getSupportFragmentManager(), new LeftMenu());
        view.setContentView(getSupportFragmentManager(), new LeftMenu());
        view.setSlidEnable(true);
        view.setMode(Mode.BOTH);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        view.toggle();
        return super.onKeyDown(keyCode, event);
    }
}
