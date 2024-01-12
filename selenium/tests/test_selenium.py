from selenium import webdriver
import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

# https://github.com/SeleniumHQ/seleniumhq.github.io/blob/trunk/examples/python/README.md

# BROWSER = 'chrome'
# BROWSER = "firefox"
BROWSER = "edge"

HOST_URL = "http://vipslogic-local.no/"
#HOST_URL = "https://logic.testvips.nibio.no"
#HOST_URL = "https://logic.vips.nibio.no"


@pytest.fixture
def browser():
    if BROWSER == "chrome":
        chrome_options = webdriver.ChromeOptions()
        chrome_options.add_argument("--headless")
        driver = webdriver.Chrome(options=chrome_options)
    elif BROWSER == "firefox":
        firefox_options = webdriver.FirefoxOptions()
        firefox_options.headless = False
        driver = webdriver.Firefox(options=firefox_options)
    elif BROWSER == "edge":
        edge_options = webdriver.EdgeOptions()
        edge_options.headless = False
        driver = webdriver.Edge(options=edge_options)
    else:
        raise Exception("Browser must be set to either chrome or firefox")

    #driver.get("http://vipslogic/index.html")
    driver.get(f"{HOST_URL}/index.html")

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
    assert link.get_attribute("href") == f"{HOST_URL}/organism"
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
    assert link.get_attribute("href") == f"{HOST_URL}/scheduling"
    link.click()
    assert browser.title == "Scheduling overview"


def test_admin_organization_group(browser):
    browser.find_element(By.XPATH, '//a[text()="Admin"]').click()
    link = WebDriverWait(browser, 10).until(
        EC.element_to_be_clickable((By.XPATH, '//a[text()="Organization groups"]'))
    )
    assert link.get_attribute("href") == f"{HOST_URL}/organizationgroup"
    link.click()
    assert browser.title == "Organization groups"


def test_admin_apple_fruit_moth(browser):
    browser.find_element(By.XPATH, '//a[text()="Admin"]').click()
    link = WebDriverWait(browser, 10).until(
        EC.element_to_be_clickable((By.XPATH, '//a[text()="Apple fruit moth"]'))
    )
    assert link.get_attribute("href") == f"{HOST_URL}/applefruitmoth"
    link.click()
    assert browser.title == "Rognebærmøllstasjoner"


def test_admin_users(browser):
    browser.find_element(By.XPATH, '//a[text()="Admin"]').click()
    link = WebDriverWait(browser, 20).until(
        EC.element_to_be_clickable((By.XPATH, '//a[text()="Users"]'))
    )
    assert link.get_attribute("href") == f"{HOST_URL}/user"
    link.click()
    WebDriverWait(browser, 20).until(
        EC.visibility_of_element_located((By.XPATH, "//h1[text()='Users']"))
    )
    assert browser.title == "Users"


def test_admin_forecasts(browser):
    browser.find_element(By.XPATH, '//a[text()="Admin"]').click()
    link = WebDriverWait(browser, 20).until(
        EC.element_to_be_clickable((By.XPATH, '//a[text()="Forecasts"]'))
    )
    assert link.get_attribute("href") == f"{HOST_URL}/forecastConfiguration"
    link.click()
    WebDriverWait(browser, 20).until(
        EC.visibility_of_element_located((By.XPATH, "//h1[text()='Forecasts']"))
    )
    assert browser.title == "Forecasts"
