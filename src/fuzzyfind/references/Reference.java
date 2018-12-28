package fuzzyfind.references;

import javafuzzysearch.utils.StrView;

import fuzzyfind.utils.Variables;

public interface Reference{
    public StrView get(Variables vars);
}
