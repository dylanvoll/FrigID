import sqlite3
import sys
from os.path import join, split

def dictionary_factory(cursor, row):
    """
    Create a dictionary from rows in a cursor result.
    The keys will be the column names.
    :param cursor: A cursor from which a query row has just been fetched
    :param row: The query row that was fetched
    :return: A dictionary associating column names to values
    """
    col_names = [d[0].lower() for d in cursor.description]
    return dict(zip(col_names, row))


def get_connection():
    dirname = split(__file__)[0]
    filename = join(dirname, "FrigID.sqlite")
    conn = sqlite3.connect(filename)
    conn.row_factory = dictionary_factory  # note: no parentheses
    return conn


def do_command(cmd, args=[]):
    conn = get_connection()
    try:
        crs = conn.cursor()
        crs.execute(cmd, args)
        rtval = crs.fetchall()
        conn.commit()
        return rtval
    finally:
        conn.close()


def do_insert(cmd, args=[]):
    conn = get_connection()
    try:
        crs = conn.cursor()
        crs.execute(cmd, args)
        rtval = crs.lastrowid
        conn.commit()
        return rtval
    finally:
        conn.close()


def do_command_no_return(cmd, args=[]):
    conn = get_connection()
    try:
        crs = conn.cursor()
        crs.execute(cmd, args)
        conn.commit()
    finally:
        conn.close()