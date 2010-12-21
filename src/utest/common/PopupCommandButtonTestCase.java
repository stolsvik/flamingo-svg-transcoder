package utest.common;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JFrame;

import org.fest.assertions.Assertions;
import org.fest.swing.edt.*;
import org.fest.swing.junit.testcase.FestSwingJUnitTestCase;
import org.fest.swing.timing.Condition;
import org.fest.swing.timing.Pause;
import org.junit.Before;
import org.junit.Test;
import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.icon.EmptyResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.common.popup.*;
import org.pushingpixels.flamingo.api.svg.SvgBatikResizableIcon;

public class PopupCommandButtonTestCase extends FestSwingJUnitTestCase {
	JFrame buttonFrame;
	JCommandButton button;
	StringBuffer stringBuffer;
	JCommandMenuButton[] menuButtons;
	final static int MENU_BUTTON_COUNT = 5;

	@Override
	@Before
	public void onSetUp() {
		URL resource = PopupCommandButtonTestCase.class.getClassLoader()
				.getResource("utest/common/edit-paste.svg");
		Assertions.assertThat(resource).isNotNull();
		final ResizableIcon icon = SvgBatikResizableIcon.getSvgIcon(resource,
				new Dimension(32, 32));
		Pause.pause(new Condition("Waiting to load the SVG icon") {
			@Override
			public boolean test() {
				return !((AsynchronousLoading) icon).isLoading();
			}
		});

		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				stringBuffer = new StringBuffer();

				buttonFrame = new JFrame();
				button = new JCommandButton("test", icon);
				button.setDisplayState(CommandButtonDisplayState.BIG);
				button.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);

				button.setPopupCallback(new PopupPanelCallback() {
					@Override
					public JPopupPanel getPopupPanel(
							JCommandButton commandButton) {
						JCommandPopupMenu result = new JCommandPopupMenu();

						menuButtons = new JCommandMenuButton[MENU_BUTTON_COUNT];
						for (int i = 0; i < MENU_BUTTON_COUNT; i++) {
							final int index = i;
							menuButtons[i] = new JCommandMenuButton("popup "
									+ i, new EmptyResizableIcon(16));
							menuButtons[i]
									.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(
												ActionEvent e) {
											stringBuffer.append(index);
										}
									});
							result.addMenuButton(menuButtons[i]);
						}

						return result;
					}
				});

				buttonFrame.setLayout(new FlowLayout());
				buttonFrame.add(button);
				buttonFrame.setSize(300, 200);
				buttonFrame.setLocationRelativeTo(null);
				buttonFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				buttonFrame.setVisible(true);
			}
		});

		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				Point locOnScreen = buttonFrame.getLocationOnScreen();
				locOnScreen.move(10, 20);
				robot().moveMouse(locOnScreen);
			}
		});
	}

	@Test
	public void activatePopupWithMouse() {
		robot().click(button);
		robot().waitForIdle();
		// popup should be showing
		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return button.getPopupModel().isPopupShowing();
			}
		})).isTrue();
	}

	@Test
	public void activatePopupWithAPI() {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				button.doPopupClick();
			}
		});
		robot().waitForIdle();
		// popup should be showing
		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return button.getPopupModel().isPopupShowing();
			}
		})).isTrue();
	}

	@Test
	public void popupDismissalByClickingOutsideTheButton() {
		robot().click(button);
		robot().waitForIdle();
		// popup should be showing
		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return button.getPopupModel().isPopupShowing();
			}
		})).isTrue();

		// move the mouse away from the button
		robot().moveMouse(button, new Point(-10, 10));
		robot().waitForIdle();
		// popup should still be visible
		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return button.getPopupModel().isPopupShowing();
			}
		})).isTrue();

		// click away from the button
		robot().click(button, new Point(-10, 10));
		robot().waitForIdle();
		// popup should be hidden
		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return button.getPopupModel().isPopupShowing();
			}
		})).isFalse();
	}

	@Test
	public void popupDismissalByClickingTheButton() {
		robot().click(button);
		robot().waitForIdle();
		// popup should be showing
		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return button.getPopupModel().isPopupShowing();
			}
		})).isTrue();

		// click the button one more time
		robot().click(button);
		robot().waitForIdle();
		// popup should be hidden
		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return button.getPopupModel().isPopupShowing();
			}
		})).isFalse();
	}

	@Test
	public void clickTheFirstMenuButton() {
		robot().click(button);
		robot().waitForIdle();

		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return menuButtons[0].isVisible();
			}
		})).isTrue();

		robot().click(menuButtons[0]);
		robot().waitForIdle();
		// popup should be hidden
		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return button.getPopupModel().isPopupShowing();
			}
		})).isFalse();

		// check that the menu button has been clicked
		Assertions.assertThat(stringBuffer.toString()).isEqualTo("0");
	}

	@Test
	public void clickTheFirstAndSecondMenuButton() {
		robot().click(button);
		robot().waitForIdle();

		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return menuButtons[0].isVisible();
			}
		})).isTrue();

		robot().click(menuButtons[0]);
		robot().waitForIdle();
		// popup should be hidden
		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return button.getPopupModel().isPopupShowing();
			}
		})).isFalse();

		// check that the menu button has been clicked
		Assertions.assertThat(stringBuffer.toString()).isEqualTo("0");

		robot().click(button);
		robot().waitForIdle();

		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return menuButtons[1].isVisible();
			}
		})).isTrue();

		robot().click(menuButtons[1]);
		robot().waitForIdle();
		// popup should be hidden
		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return button.getPopupModel().isPopupShowing();
			}
		})).isFalse();

		// check that the menu button has been clicked
		Assertions.assertThat(stringBuffer.toString()).isEqualTo("01");
	}

	@Test
	public void clickAllMenuButtons() {
		for (int i = 0; i < MENU_BUTTON_COUNT; i++) {
			robot().click(button);
			robot().waitForIdle();
			final int index = i;
			Assertions.assertThat(
					GuiActionRunner.execute(new GuiQuery<Boolean>() {
						@Override
						protected Boolean executeInEDT() throws Throwable {
							return menuButtons[index].isVisible();
						}
					})).isTrue();

			robot().click(menuButtons[i]);
			robot().waitForIdle();
			// popup should be hidden
			Assertions.assertThat(
					GuiActionRunner.execute(new GuiQuery<Boolean>() {
						@Override
						protected Boolean executeInEDT() throws Throwable {
							return button.getPopupModel().isPopupShowing();
						}
					})).isFalse();
		}

		String expected = "";
		for (int i = 0; i < MENU_BUTTON_COUNT; i++) {
			expected += i;
		}

		// check that the menu buttons have been clicked
		Assertions.assertThat(stringBuffer.toString()).isEqualTo(expected);
	}

	@Test
	public void checkPopupDisable() {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				button.getPopupModel().setEnabled(false);
			}
		});
		robot().waitForIdle();

		// popup should be disabled
		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return button.getPopupModel().isEnabled();
			}
		})).isFalse();

		// try clicking
		robot().click(button);
		robot().waitForIdle();
		// popup should be hidden
		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return button.getPopupModel().isPopupShowing();
			}
		})).isFalse();

		// now enable the popup
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				button.getPopupModel().setEnabled(true);
			}
		});
		robot().waitForIdle();

		// popup should be enabled
		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return button.getPopupModel().isEnabled();
			}
		})).isTrue();

		// try clicking
		robot().click(button);
		robot().waitForIdle();
		// popup should be shown
		Assertions.assertThat(GuiActionRunner.execute(new GuiQuery<Boolean>() {
			@Override
			protected Boolean executeInEDT() throws Throwable {
				return button.getPopupModel().isPopupShowing();
			}
		})).isTrue();
	}
}
