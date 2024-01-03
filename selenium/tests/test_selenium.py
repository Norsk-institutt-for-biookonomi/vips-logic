from selenium import webdriver
import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

# https://github.com/SeleniumHQ/seleniumhq.github.io/blob/trunk/examples/python/README.md


@pytest.fixture
def browser():
    chrome_options = webdriver.ChromeOptions()
    # Uncomment line below to prevent Chrome from opening
    # chrome_options.add_argument("--headless")
    driver = webdriver.Chrome(options=chrome_options)

    driver.get("http://localhost/index.html")
    assert driver.title == "Please log in"
    driver.implicitly_wait(0.5)
    driver.find_element(by=By.NAME, value="username").send_keys("seleniumtest")
    driver.find_element(by=By.NAME, value="password").send_keys("seleniumTEST")
    driver.find_element(by=By.CSS_SELECTOR, value="button").click()
    assert driver.title == "Welcome to VIPSLogic"
    yield driver
    driver.quit()


def test_name_of_logged_in_user(browser):
    link = browser.find_element(By.XPATH, '//a[text()="Selenium Testbruker"]')
    assert link is not None


def test_admin_organisms(browser):
    browser.find_element(By.XPATH, '//a[text()="Admin"]').click()
    link = WebDriverWait(browser, 15).until(
        EC.element_to_be_clickable((By.XPATH, '//a[text()="Organisms"]'))
    )
    assert link.get_attribute("href") == "http://localhost/organism"
    link.click()
    assert browser.title == "Organisms"
    link = WebDriverWait(browser, 15).until(
        EC.element_to_be_clickable((By.XPATH, '//a[text()="List all pests"]'))
    )
    link.click()
    assert browser.title == "All pests"


def test_admin_scheduling(browser):
    browser.find_element(By.XPATH, '//a[text()="Admin"]').click()
    link = WebDriverWait(browser, 10).until(
        EC.element_to_be_clickable((By.XPATH, '//a[text()="Scheduling"]'))
    )
    assert link.get_attribute("href") == "http://localhost/scheduling"
    link.click()
    assert browser.title == "Scheduling overview"


def test_admin_organization_group(browser):
    browser.find_element(By.XPATH, '//a[text()="Admin"]').click()
    link = WebDriverWait(browser, 10).until(
        EC.element_to_be_clickable((By.XPATH, '//a[text()="Organization groups"]'))
    )
    assert link.get_attribute("href") == "http://localhost/organizationgroup"
    link.click()
    assert browser.title == "Organization groups"


def test_admin_apple_fruit_moth(browser):
    browser.find_element(By.XPATH, '//a[text()="Admin"]').click()
    link = WebDriverWait(browser, 10).until(
        EC.element_to_be_clickable((By.XPATH, '//a[text()="Apple fruit moth"]'))
    )
    assert link.get_attribute("href") == "http://localhost/applefruitmoth"
    link.click()
    assert browser.title == "Rognebærmøllstasjoner"


def test_admin_users(browser):
    browser.find_element(By.XPATH, '//a[text()="Admin"]').click()
    link = WebDriverWait(browser, 20).until(
        EC.element_to_be_clickable((By.XPATH, '//a[text()="Users"]'))
    )
    assert link.get_attribute("href") == "http://localhost/user"
    link.click()
    assert browser.title == "Users"


def test_admin_forecasts(browser):
    browser.find_element(By.XPATH, '//a[text()="Admin"]').click()
    link = WebDriverWait(browser, 20).until(
        EC.element_to_be_clickable((By.XPATH, '//a[text()="Forecasts"]'))
    )
    assert link.get_attribute("href") == "http://localhost/forecastConfiguration"
    link.click()
    assert browser.title == "Forecasts"
